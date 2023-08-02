/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.gui.impl.page.admin.role.mining.algorithm.utils;

import static com.evolveum.midpoint.gui.impl.page.admin.role.mining.algorithm.utils.ExtractPatternUtils.addDetectedObjectIntersection;
import static com.evolveum.midpoint.gui.impl.page.admin.role.mining.algorithm.utils.ExtractPatternUtils.addDetectedObjectJaccard;
import static com.evolveum.midpoint.gui.impl.page.admin.role.mining.utils.ClusterObjectUtils.*;
import static com.evolveum.midpoint.gui.impl.page.admin.role.mining.utils.Tools.endTimer;
import static com.evolveum.midpoint.gui.impl.page.admin.role.mining.utils.Tools.startTimer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.repo.api.RepositoryService;

import com.google.common.collect.ListMultimap;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.impl.page.admin.role.mining.algorithm.object.ClusterOptions;
import com.evolveum.midpoint.gui.impl.page.admin.role.mining.algorithm.object.ClusterStatistic;
import com.evolveum.midpoint.gui.impl.page.admin.role.mining.algorithm.object.DataPoint;
import com.evolveum.midpoint.gui.impl.page.admin.role.mining.algorithm.detection.DetectedPattern;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

public class ClusterAlgorithmUtils {

    @NotNull
    public List<PrismObject<RoleAnalysisClusterType>> processClusters(PageBase pageBase, List<DataPoint> dataPoints,
            List<Cluster<DataPoint>> clusters, ClusterOptions clusterOptions) {
        long start;

        QName complexType = clusterOptions.getMode().equals(RoleAnalysisProcessModeType.ROLE)
                ? RoleType.COMPLEX_TYPE
                : UserType.COMPLEX_TYPE;

        start = startTimer("generate clusters mp objects");
        List<PrismObject<RoleAnalysisClusterType>> clusterTypeObjectWithStatistic = IntStream.range(0, clusters.size())
                .mapToObj(i -> prepareClusters(pageBase, clusters.get(i).getPoints(), String.valueOf(i),
                        dataPoints, clusterOptions, complexType))
                .collect(Collectors.toList());
        endTimer(start, "generate clusters mp objects");

        start = startTimer("generate outliers mp objects");
        PrismObject<RoleAnalysisClusterType> clusterTypePrismObject = prepareOutlierClusters(pageBase, dataPoints, complexType);
        clusterTypeObjectWithStatistic.add(clusterTypePrismObject);
        endTimer(start, "generate outliers mp objects");
        return clusterTypeObjectWithStatistic;
    }

    @NotNull
    public List<PrismObject<RoleAnalysisClusterType>> processExactMatch(PageBase pageBase, List<DataPoint> dataPoints,
            ClusterOptions clusterOptions) {
        long start;

        QName processedObjectComplexType = clusterOptions.getMode().equals(RoleAnalysisProcessModeType.ROLE)
                ? RoleType.COMPLEX_TYPE
                : UserType.COMPLEX_TYPE;

        QName propertiesComplexType = processedObjectComplexType.equals(RoleType.COMPLEX_TYPE)
                ? UserType.COMPLEX_TYPE
                : RoleType.COMPLEX_TYPE;

        start = startTimer("generate clusters mp objects");
        List<DataPoint> dataPointsOutliers = new ArrayList<>();
        int size = dataPoints.size();
        List<PrismObject<RoleAnalysisClusterType>> clusterTypeObjectWithStatistic = IntStream.range(0, size)
                .mapToObj(i -> exactPrepareDataPoints(pageBase, dataPoints.get(i), String.valueOf(i),
                        clusterOptions, dataPointsOutliers, processedObjectComplexType, propertiesComplexType))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        endTimer(start, "generate clusters mp objects");

        start = startTimer("generate outliers mp objects");
        PrismObject<RoleAnalysisClusterType> clusterTypePrismObject = prepareOutlierClusters(pageBase, dataPoints, processedObjectComplexType);
        clusterTypeObjectWithStatistic.add(clusterTypePrismObject);
        endTimer(start, "generate outliers mp objects");
        return clusterTypeObjectWithStatistic;
    }

    public static List<DataPoint> prepareDataPoints(ListMultimap<List<String>, String> chunkMap) {
        List<DataPoint> dataPoints = new ArrayList<>();

        for (List<String> points : chunkMap.keySet()) {
            List<String> elements = chunkMap.get(points);
            double[] vectorPoints = new double[points.size()];
            for (int i = 0; i < points.size(); i++) {
                String point = points.get(i);
                vectorPoints[i] = UUIDToDoubleConverter.convertUUid(point);
            }
            dataPoints.add(new DataPoint(vectorPoints, elements, points));

        }
        return dataPoints;
    }

    private ClusterStatistic statisticLoad(List<DataPoint> clusterDataPoints, List<DataPoint> allDataPoints,
            String clusterIndex, QName complexType, PageBase pageBase) {

        int totalPoints = 0;
        int minVectorPoint = Integer.MAX_VALUE;
        int maxVectorPoint = -1;

        Set<String> elementsOids = new HashSet<>();
        Set<String> occupiedPoints = new HashSet<>();

        for (DataPoint clusterDataPoint : clusterDataPoints) {
            allDataPoints.remove(clusterDataPoint);
            List<String> points = clusterDataPoint.getPoints();
            List<String> elements = clusterDataPoint.getElements();
            elementsOids.addAll(elements);
            occupiedPoints.addAll(points);
        }

        PolyStringType name = PolyStringType.fromOrig("cluster_" + clusterIndex);

        RepositoryService repositoryService = pageBase.getRepositoryService();
        OperationResult operationResult = new OperationResult("prepare");
        Set<ObjectReferenceType> processedObjectsRef = createObjectReferences(elementsOids, complexType, repositoryService,
                operationResult);

        List<String> existingProperties = checkExist(occupiedPoints, repositoryService, operationResult);
        int propertiesCount = existingProperties.size();

        for (DataPoint clusterDataPoint : clusterDataPoints) {
            List<String> points = clusterDataPoint.getPoints();
            points.retainAll(existingProperties);

            int pointsCount = points.size();
            totalPoints += pointsCount;
            minVectorPoint = Math.min(minVectorPoint, pointsCount);
            maxVectorPoint = Math.max(maxVectorPoint, pointsCount);

        }

        int membersCount = processedObjectsRef.size();

        double meanPoints = (double) totalPoints / membersCount;

        double density = Math.min((totalPoints / (double) (membersCount * propertiesCount)) * 100, 100);

        if (propertiesCount == 0 || membersCount == 0) {
            return null;
        }

        return new ClusterStatistic(name, processedObjectsRef, membersCount, propertiesCount, minVectorPoint,
                maxVectorPoint, meanPoints, density);
    }

    private ClusterStatistic exactStatisticLoad(DataPoint clusterDataPoints,
            String clusterIndex, int minGroupSize, List<DataPoint> dataPointsOutliers, QName processedObjectComplexType,
            QName propertiesComplexType, PageBase pageBase) {

        Set<String> elementsOids = new HashSet<>(clusterDataPoints.getElements());
        Set<String> occupiedPoints = new HashSet<>(clusterDataPoints.getPoints());

        if (elementsOids.size() < minGroupSize) {
            dataPointsOutliers.add(clusterDataPoints);
            return null;
        }

        PolyStringType name = PolyStringType.fromOrig("cluster_" + clusterIndex);

        RepositoryService repositoryService = pageBase.getRepositoryService();
        OperationResult operationResult = new OperationResult("prepare");

        Set<ObjectReferenceType> membersObjectsRef = createObjectReferences(elementsOids, processedObjectComplexType,
                repositoryService, operationResult);

        Set<ObjectReferenceType> propertiesObjectRef = createObjectReferences(occupiedPoints, propertiesComplexType,
                repositoryService, operationResult);

        double density = 100;

        int membersCount = membersObjectsRef.size();
        int propertiesCount = propertiesObjectRef.size();

        if (propertiesCount == 0 || membersCount == 0) {
            return null;
        }

        return new ClusterStatistic(name, propertiesObjectRef, membersObjectsRef, membersCount, propertiesCount,
                propertiesCount, propertiesCount, propertiesCount, density);
    }

    private PrismObject<RoleAnalysisClusterType> exactPrepareDataPoints(PageBase pageBase, DataPoint dataPointCluster,
            String clusterIndex, ClusterOptions clusterOptions, List<DataPoint> dataPointsOutliers,
            QName processedObjectComplexType, QName propertiesComplexType) {

        int minGroupSize = clusterOptions.getMinGroupSize();
        ClusterStatistic clusterStatistic = exactStatisticLoad(dataPointCluster, clusterIndex, minGroupSize,
                dataPointsOutliers, processedObjectComplexType, propertiesComplexType, pageBase);

        if (clusterStatistic != null) {
            RoleAnalysisClusterStatisticType roleAnalysisClusterStatisticType = createClusterStatisticType(clusterStatistic);

            return generateClusterObject(pageBase, clusterStatistic, clusterOptions, roleAnalysisClusterStatisticType);
        } else {return null;}
    }

    private PrismObject<RoleAnalysisClusterType> prepareClusters(PageBase pageBase, List<DataPoint> dataPointCluster,
            String clusterIndex, List<DataPoint> dataPoints, ClusterOptions clusterOptions, QName complexType) {

        ClusterStatistic clusterStatistic = statisticLoad(dataPointCluster, dataPoints, clusterIndex, complexType, pageBase);

        assert clusterStatistic != null;
        RoleAnalysisClusterStatisticType roleAnalysisClusterStatisticType = createClusterStatisticType(clusterStatistic);

        return generateClusterObject(pageBase, clusterStatistic, clusterOptions, roleAnalysisClusterStatisticType);

    }

    private PrismObject<RoleAnalysisClusterType> prepareOutlierClusters(PageBase pageBase, List<DataPoint> dataPoints,
            QName complexType) {

        int minVectorPoint = Integer.MAX_VALUE;
        int maxVectorPoint = -1;

        int totalDataPoints = dataPoints.size();
        int sumPoints = 0;

        Set<String> elementsOid = new HashSet<>();
        Set<String> pointsSet = new HashSet<>();
        for (DataPoint dataPoint : dataPoints) {
            List<String> points = dataPoint.getPoints();
            pointsSet.addAll(points);
            elementsOid.addAll(dataPoint.getElements());

            int pointsSize = points.size();
            sumPoints += pointsSize;
            minVectorPoint = Math.min(minVectorPoint, pointsSize);
            maxVectorPoint = Math.max(maxVectorPoint, pointsSize);
        }

        double meanPoints = (double) sumPoints / totalDataPoints;

        int pointsSize = pointsSet.size();
        int elementSize = elementsOid.size();
        double density = (sumPoints / (double) (elementSize * pointsSize)) * 100;

        PolyStringType name = PolyStringType.fromOrig("outliers");

        Set<ObjectReferenceType> processedObjectsRef = new HashSet<>();
        ObjectReferenceType objectReferenceType;
        for (String element : elementsOid) {
            objectReferenceType = new ObjectReferenceType();
            objectReferenceType.setType(complexType);
            objectReferenceType.setOid(element);
            processedObjectsRef.add(objectReferenceType);
        }

        ClusterStatistic clusterStatistic = new ClusterStatistic(name, processedObjectsRef, elementSize,
                pointsSize, minVectorPoint, maxVectorPoint, meanPoints, density);

        RoleAnalysisClusterStatisticType roleAnalysisClusterStatisticType = createClusterStatisticType(clusterStatistic);

        return generateClusterObject(pageBase, clusterStatistic, null, roleAnalysisClusterStatisticType);
    }

    private @NotNull PrismObject<RoleAnalysisClusterType> generateClusterObject(PageBase pageBase,
            ClusterStatistic clusterStatistic,
            ClusterOptions clusterOptions,
            RoleAnalysisClusterStatisticType roleAnalysisClusterStatisticType) {

        PrismObject<RoleAnalysisClusterType> clusterTypePrismObject = prepareClusterPrismObject(pageBase);
        assert clusterTypePrismObject != null;

        Set<ObjectReferenceType> members = clusterStatistic.getMembersRef();

        RoleAnalysisClusterType clusterType = clusterTypePrismObject.asObjectable();
        clusterType.setOid(String.valueOf(UUID.randomUUID()));
        clusterType.setClusterStatistic(roleAnalysisClusterStatisticType);
        clusterType.getMember().addAll(members);
        clusterType.setName(clusterStatistic.getName());

        if (clusterOptions != null) {
            RoleAnalysisProcessModeType mode = clusterOptions.getMode();
            DefaultPatternResolver defaultPatternResolver = new DefaultPatternResolver(mode);

            OperationResult operationResult = new OperationResult("loadPattern");
            List<RoleAnalysisDetectionType> roleAnalysisClusterDetectionTypeList = defaultPatternResolver
                    .loadPattern(clusterOptions, clusterStatistic, clusterType, pageBase, operationResult);

            clusterType.getDetection().addAll(roleAnalysisClusterDetectionTypeList);
        }

        return clusterTypePrismObject;
    }

    public static List<DetectedPattern> transformDefaultPattern(RoleAnalysisClusterType clusterType) {
        List<RoleAnalysisDetectionType> defaultDetection = clusterType.getDetection();
        List<DetectedPattern> mergedIntersection = new ArrayList<>();
        for (RoleAnalysisDetectionType roleAnalysisClusterDetectionType : defaultDetection) {
            RoleAnalysisSearchModeType searchMode = roleAnalysisClusterDetectionType.getSearchMode();

            List<ObjectReferenceType> propertiesRef = roleAnalysisClusterDetectionType.getPropertiesOccupation();
            List<ObjectReferenceType> membersObject = roleAnalysisClusterDetectionType.getMemberOccupation();

            Set<String> members = new HashSet<>();
            for (ObjectReferenceType objectReferenceType : membersObject) {
                members.add(objectReferenceType.getOid());
            }

            Set<String> properties = new HashSet<>();
            for (ObjectReferenceType objectReferenceType : propertiesRef) {
                properties.add(objectReferenceType.getOid());
            }

            if (searchMode.equals(RoleAnalysisSearchModeType.JACCARD)) {
                mergedIntersection.add(addDetectedObjectJaccard(properties, members, null));
            } else {
                mergedIntersection.add(addDetectedObjectIntersection(properties,
                        members, null));
            }
        }

        return mergedIntersection;
    }

    public static List<RoleAnalysisDetectionType> loadIntersections(List<DetectedPattern> possibleBusinessRole,
            RoleAnalysisSearchModeType searchMode, QName processedObjectComplexType, QName propertiesComplexType) {
        List<RoleAnalysisDetectionType> roleAnalysisClusterDetectionTypeList = new ArrayList<>();

        if (searchMode.equals(RoleAnalysisSearchModeType.JACCARD)) {
            loadJaccardIntersection(possibleBusinessRole,
                    searchMode, roleAnalysisClusterDetectionTypeList, processedObjectComplexType, propertiesComplexType);
        } else {
            loadSimpleIntersection(possibleBusinessRole,
                    searchMode, roleAnalysisClusterDetectionTypeList, processedObjectComplexType, propertiesComplexType);
        }
        return roleAnalysisClusterDetectionTypeList;
    }

    private static void loadJaccardIntersection(List<DetectedPattern> possibleBusinessRole, RoleAnalysisSearchModeType searchMode,
            List<RoleAnalysisDetectionType> roleAnalysisClusterDetectionTypeList,
            QName processedObjectComplexType, QName propertiesComplexType) {
        RoleAnalysisDetectionType roleAnalysisClusterDetectionType;
        for (DetectedPattern detectedPattern : possibleBusinessRole) {
            roleAnalysisClusterDetectionType = new RoleAnalysisDetectionType();
            roleAnalysisClusterDetectionType.setSearchMode(searchMode);

            ObjectReferenceType objectReferenceType;
            Set<String> members = detectedPattern.getMembers();
            for (String propertiesRef : members) {
                objectReferenceType = new ObjectReferenceType();
                objectReferenceType.setOid(propertiesRef);
                objectReferenceType.setType(processedObjectComplexType);
                roleAnalysisClusterDetectionType.getMemberOccupation().add(objectReferenceType);

            }

            Set<String> properties = detectedPattern.getProperties();
            for (String propertiesRef : properties) {
                objectReferenceType = new ObjectReferenceType();
                objectReferenceType.setOid(propertiesRef);
                objectReferenceType.setType(propertiesComplexType);
                roleAnalysisClusterDetectionType.getPropertiesOccupation().add(objectReferenceType);

            }

            roleAnalysisClusterDetectionType.setClusterMetric(detectedPattern.getClusterMetric());
            roleAnalysisClusterDetectionTypeList.add(roleAnalysisClusterDetectionType);
        }
    }

    private static void loadSimpleIntersection(List<DetectedPattern> possibleBusinessRole,
            RoleAnalysisSearchModeType searchMode, List<RoleAnalysisDetectionType> roleAnalysisClusterDetectionTypeList,
            QName processedObjectComplexType, QName propertiesComplexType) {
        RoleAnalysisDetectionType roleAnalysisClusterDetectionType;
        for (DetectedPattern detectedPattern : possibleBusinessRole) {
            roleAnalysisClusterDetectionType = new RoleAnalysisDetectionType();
            roleAnalysisClusterDetectionType.setSearchMode(searchMode);

            ObjectReferenceType objectReferenceType;
            Set<String> members = detectedPattern.getMembers();
            for (String memberRef : members) {
                objectReferenceType = new ObjectReferenceType();
                objectReferenceType.setOid(memberRef);
                objectReferenceType.setType(processedObjectComplexType);
                roleAnalysisClusterDetectionType.getMemberOccupation().add(objectReferenceType);

            }

            Set<String> properties = detectedPattern.getProperties();
            for (String propertiesRef : properties) {
                objectReferenceType = new ObjectReferenceType();
                objectReferenceType.setOid(propertiesRef);
                objectReferenceType.setType(propertiesComplexType);
                roleAnalysisClusterDetectionType.getPropertiesOccupation().add(objectReferenceType);
            }

            roleAnalysisClusterDetectionType.setClusterMetric(detectedPattern.getClusterMetric());
            roleAnalysisClusterDetectionTypeList.add(roleAnalysisClusterDetectionType);
        }
    }

}
