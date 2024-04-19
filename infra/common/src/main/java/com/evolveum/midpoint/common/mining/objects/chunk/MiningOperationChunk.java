/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.common.mining.objects.chunk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.common.mining.utils.algorithm.JaccardSorter;
import com.evolveum.midpoint.common.mining.utils.values.RoleAnalysisOperationMode;
import com.evolveum.midpoint.common.mining.utils.values.RoleAnalysisSortMode;

import static com.evolveum.midpoint.common.mining.utils.algorithm.JaccardSorter.jacquardSimilarity;

/**
 * <p>
 * The `MiningOperationChunk` class represents a chunk of data used in the role analysis process. It contains two lists:
 * - `miningUserTypeChunks` for user data
 * - `miningRoleTypeChunks` for role data
 * </p>
 * <p>
 * This class provides methods to retrieve these lists and sort them based on the specified `RoleAnalysisSortMode`.
 * Sorting is performed by chunk, so the lists are sorted independently of each other.
 * </p>
 */
public class MiningOperationChunk implements Serializable {

    private List<MiningUserTypeChunk> miningUserTypeChunks;
    private List<MiningRoleTypeChunk> miningRoleTypeChunks;
    RoleAnalysisSortMode sortModeUserChunk = RoleAnalysisSortMode.NONE;
    RoleAnalysisSortMode sortModeRoleChunk = RoleAnalysisSortMode.NONE;

    public MiningOperationChunk(List<MiningUserTypeChunk> miningUserTypeChunks, List<MiningRoleTypeChunk> miningRoleTypeChunks) {
        resetList();
        this.miningUserTypeChunks = miningUserTypeChunks;
        this.miningRoleTypeChunks = miningRoleTypeChunks;
    }

    public List<MiningUserTypeChunk> getSimpleMiningUserTypeChunks() {
        return miningUserTypeChunks;
    }

    public List<MiningRoleTypeChunk> getSimpleMiningRoleTypeChunks() {
        return miningRoleTypeChunks;
    }

    public List<MiningUserTypeChunk> getMiningUserTypeChunks(@NotNull RoleAnalysisSortMode roleAnalysisSortMode) {
        this.sortModeUserChunk = roleAnalysisSortMode;
        if (roleAnalysisSortMode.equals(RoleAnalysisSortMode.JACCARD)) {
            this.miningUserTypeChunks = JaccardSorter.jaccardSorter(miningUserTypeChunks);
        } else if (roleAnalysisSortMode.equals(RoleAnalysisSortMode.FREQUENCY)) {
            this.miningUserTypeChunks = JaccardSorter.frequencyBasedSort(miningUserTypeChunks);
        }
        return sortByIncludeStatusMiningUserTypeChunks();
    }

    public List<MiningRoleTypeChunk> getMiningRoleTypeChunks(@NotNull RoleAnalysisSortMode roleAnalysisSortMode) {
        this.sortModeRoleChunk = roleAnalysisSortMode;
        if (roleAnalysisSortMode.equals(RoleAnalysisSortMode.JACCARD)) {
            this.miningRoleTypeChunks = JaccardSorter.jaccardSorter(miningRoleTypeChunks);
        } else if (roleAnalysisSortMode.equals(RoleAnalysisSortMode.FREQUENCY)) {
            this.miningRoleTypeChunks = JaccardSorter.frequencyBasedSort(miningRoleTypeChunks);
        }
        return sortByStatusIncludeMiningRoleTypeChunks();
    }

    //TODO check it. it should be executed only when pattern or candidate role is selected.
    public List<MiningRoleTypeChunk> sortByStatusIncludeMiningRoleTypeChunks() {
        RoleAnalysisOperationMode thisStatusFirst = RoleAnalysisOperationMode.INCLUDE;
        this.miningRoleTypeChunks.sort((chunk1, chunk2) -> {
            if (chunk1.getStatus() == thisStatusFirst && chunk2.getStatus() != thisStatusFirst) {
                return -1;
            } else if (chunk1.getStatus() != thisStatusFirst && chunk2.getStatus() == thisStatusFirst) {
                return 1;
            } else {
                return chunk1.getStatus().compareTo(chunk2.getStatus());
            }
        });

        Comparator<MiningRoleTypeChunk> jaccardComparator = (chunk1, chunk2) -> {
            List<String> propertiesA = chunk1.getProperties();
            List<String> propertiesB = chunk2.getProperties();
            double similarity1 = jacquardSimilarity(propertiesA, propertiesB);
            double similarity2 = jacquardSimilarity(propertiesA, propertiesB);
            return Double.compare(similarity2, similarity1);
        };

        miningRoleTypeChunks.subList(0, Collections.frequency(miningRoleTypeChunks, thisStatusFirst))
                .sort(jaccardComparator);

        return miningRoleTypeChunks;
    }

    //TODO check it. it should be executed only when pattern or candidate role is selected.
    public List<MiningUserTypeChunk> sortByIncludeStatusMiningUserTypeChunks() {
        RoleAnalysisOperationMode thisStatusFirst = RoleAnalysisOperationMode.INCLUDE;
        this.miningUserTypeChunks.sort((chunk1, chunk2) -> {
            if (chunk1.getStatus() == thisStatusFirst && chunk2.getStatus() != thisStatusFirst) {
                return -1;
            } else if (chunk1.getStatus() != thisStatusFirst && chunk2.getStatus() == thisStatusFirst) {
                return 1;
            } else {
                return chunk1.getStatus().compareTo(chunk2.getStatus());
            }
        });


        Comparator<MiningUserTypeChunk> jaccardComparator = (chunk1, chunk2) -> {
            List<String> propertiesA = chunk1.getProperties();
            List<String> propertiesB = chunk2.getProperties();
            double similarity1 = jacquardSimilarity(propertiesA, propertiesB);
            double similarity2 = jacquardSimilarity(propertiesA, propertiesB);
            return Double.compare(similarity2, similarity1);
        };

        miningUserTypeChunks.subList(0, Collections.frequency(miningUserTypeChunks, thisStatusFirst))
                .sort(jaccardComparator);

        return miningUserTypeChunks;
    }

    private void resetList() {
        miningUserTypeChunks = new ArrayList<>();
        miningRoleTypeChunks = new ArrayList<>();
    }

    public RoleAnalysisSortMode getSortModeUserChunk() {
        return sortModeUserChunk;
    }

    public RoleAnalysisSortMode getSortModeRoleChunk() {
        return sortModeRoleChunk;
    }

}
