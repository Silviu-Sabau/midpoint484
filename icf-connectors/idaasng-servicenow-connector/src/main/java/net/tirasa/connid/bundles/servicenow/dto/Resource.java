/**
 * Copyright © 2018 ConnId (connid-dev@googlegroups.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.connid.bundles.servicenow.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.tirasa.connid.bundles.servicenow.utils.SNAttributes;
import net.tirasa.connid.bundles.servicenow.utils.SNUtils;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;

public class Resource implements BaseEntity {

    @JsonIgnore
    private static final Log LOG = Log.getLog(BaseEntity.class);

    @JsonProperty("sys_id")
    private String sysId;

    @JsonProperty("upon_approval")
    private String uponApproval;

    @JsonProperty("expected_start")
    private String expectedStart;

    @JsonProperty("reopen_count")
    private String reopenCount;

    @JsonProperty("close_notes")
    private String closeNotes;

    @JsonProperty("additional_assignee_list")
    private String additionalAssigneeList;

    @JsonProperty("impact")
    private String impact;

    @JsonProperty("urgency")
    private String urgency;

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("sys_tags")
    private String sysTags;

    @JsonProperty("description")
    private String description;

    @JsonProperty("group_list")
    private String groupList;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("delivery_plan")
    private String deliveryPlan;

    @JsonProperty("sys_mod_count")
    private String sysModCount;

    @JsonProperty("work_notes_list")
    private String workNotesList;

    @JsonProperty("business_service")
    private String businessService;

    @JsonProperty("follow_up")
    private String followUp;

    @JsonProperty("closed_at")
    private String closedAt;

    @JsonProperty("sla_due")
    private String slaDue;

    @JsonProperty("delivery_task")
    private String deliveryTask;

    @JsonProperty("sys_updated_on")
    private String sysUpdatedOn;

    @JsonProperty("work_end")
    private String workEnd;

    @JsonProperty("number")
    private String number;

    @JsonProperty("closed_by")
    private String closedBy;

    @JsonProperty("work_start")
    private String workStart;

    @JsonProperty("calendar_stc")
    private String calendarStc;

    @JsonProperty("category")
    private String category;

    @JsonProperty("business_duration")
    private String businessDuration;

    @JsonProperty("incident_state")
    private String incidentState;

    @JsonProperty("activity_due")
    private String activityDue;

    @JsonProperty("correlation_display")
    private String correlationDisplay;

    @JsonProperty("active")
    private String active;

    @JsonProperty("due_date")
    private String dueDate;

    @JsonProperty("knowledge")
    private String knowledge;

    @JsonProperty("made_sla")
    private String madeSla;

    @JsonProperty("comments_and_work_notes")
    private String commentsAndWorkNotes;

    @JsonProperty("parent_incident")
    private String parentIncident;

    @JsonProperty("state")
    private String state;

    @JsonProperty("user_input")
    private String userInput;

    @JsonProperty("sys_created_on")
    private String sysCreatedOn;

    @JsonProperty("approval_set")
    private String approvalSet;

    @JsonProperty("reassignment_count")
    private String reassignmentCount;

    @JsonProperty("rfc")
    private String rfc;

    @JsonProperty("child_incidents")
    private String childIncidents;

    @JsonProperty("opened_at")
    private String openedAt;

    @JsonProperty("short_description")
    private String shortDescription;

    @JsonProperty("order")
    private String order;

    @JsonProperty("sys_updated_by")
    private String sysUpdatedBy;

    @JsonProperty("resolved_by")
    private String resolvedBy;

    @JsonProperty("notify")
    private String notify;

    @JsonProperty("upon_reject")
    private String uponReject;

    @JsonProperty("approval_history")
    private String approvalHistory;

    @JsonProperty("problem_id")
    private String problemId;

    @JsonProperty("work_notes")
    private String workNotes;

    @JsonProperty("calendar_duration")
    private String calendarDuration;

    @JsonProperty("close_code")
    private String closeCode;

    @JsonProperty("approval")
    private String approval;

    @JsonProperty("caused_by")
    private String causedBy;

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("sys_created_by")
    private String sysCreatedBy;

    @JsonProperty("resolved_at")
    private String resolvedAt;

    @JsonProperty("assigned_to")
    private String assignedTo;

    @JsonProperty("business_stc")
    private String businessStc;

    @JsonProperty("wf_activity")
    private String wfActivity;

    @JsonProperty("sys_domain_path")
    private String sysDomainPath;

    @JsonProperty("subcategory")
    private String subcategory;

    @JsonProperty("rejection_goto")
    private String rejectionGoto;

    @JsonProperty("sys_class_name")
    private String sysClassName;

    @JsonProperty("watch_list")
    private String watchList;

    @JsonProperty("time_worked")
    private String timeWorked;

    @JsonProperty("contact_type")
    private String contactType;

    @JsonProperty("escalation")
    private String escalation;

    @JsonProperty("comments")
    private String comments;

    @JsonProperty("calendar_integration")
    private String calendarIntegration;

    @JsonProperty("country")
    private String country;

    @JsonProperty("user_password")
    private String userPassword;

    @JsonProperty("last_login_time")
    private String lastLoginTime;

    @JsonProperty("source")
    private String source;

    @JsonProperty("building")
    private String building;

    @JsonProperty("web_service_access_only")
    private String webServiceAccessOnly;

    @JsonProperty("notification")
    private String notification;

    @JsonProperty("enable_multifactor_authn")
    private String enableMultifactorAuthn;

    @JsonProperty("vip")
    private String vip;

    @JsonProperty("zip")
    private String zip;

    @JsonProperty("home_phone")
    private String homePhone;

    @JsonProperty("time_format")
    private String timeFormat;

    @JsonProperty("last_login")
    private String lastLogin;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("name")
    private String name;

    @JsonProperty("employee_number")
    private String employeeNumber;

    @JsonProperty("password_needs_reset")
    private String passwordNeedsReset;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("city")
    private String city;

    @JsonProperty("failed_attempts")
    private String failedAttempts;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("roles")
    private String roles;

    @JsonProperty("title")
    private String title;

    @JsonProperty("internal_integration_user")
    private String internalIntegrationUser;

    @JsonProperty("ldap_server")
    private String ldapServer;

    @JsonProperty("mobile_phone")
    private String mobilePhone;

    @JsonProperty("street")
    private String street;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("introduction")
    private String introduction;

    @JsonProperty("preferred_language")
    private String preferredLanguage;

    @JsonProperty("locked_out")
    private String lockedOut;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("photo")
    private String photo;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("time_zone")
    private String timeZone;

    @JsonProperty("schedule")
    private SNComplex schedule;

    @JsonProperty("date_format")
    private String dateFormat;

    @JsonProperty("manager")
    private SNComplex manager;

    @JsonProperty("department")
    private SNComplex department;

    @JsonProperty("cost_center")
    private SNComplex costCenter;

    @JsonProperty("opened_by")
    private SNComplex openedBy;

    @JsonProperty("sys_domain")
    private SNComplex sysDomain;

    @JsonProperty("caller_id")
    private SNComplex callerId;

    @JsonProperty("location")
    private SNComplex location;

    @JsonProperty("company")
    private SNComplex company;

    @JsonProperty("assignment_group")
    private SNComplex assignmentGroup;

    @JsonProperty("cmdb_ci")
    private SNComplex cmdbCi;

    @JsonProperty("default_perspective")
    private SNComplex defaultPerspective;

    @JsonProperty("parent")
    private SNComplex parent;

    public String getUponApproval() {
        return uponApproval;
    }

    public void setUponApproval(final String uponApproval) {
        this.uponApproval = uponApproval;
    }

    public String getExpectedStart() {
        return expectedStart;
    }

    public void setExpectedStart(final String expectedStart) {
        this.expectedStart = expectedStart;
    }

    public String getReopenCount() {
        return reopenCount;
    }

    public void setReopenCount(final String reopenCount) {
        this.reopenCount = reopenCount;
    }

    public String getCloseNotes() {
        return closeNotes;
    }

    public void setCloseNotes(final String closeNotes) {
        this.closeNotes = closeNotes;
    }

    public String getAdditionalAssigneeList() {
        return additionalAssigneeList;
    }

    public void setAdditionalAssigneeList(final String additionalAssigneeList) {
        this.additionalAssigneeList = additionalAssigneeList;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(final String impact) {
        this.impact = impact;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(final String urgency) {
        this.urgency = urgency;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(final String correlationId) {
        this.correlationId = correlationId;
    }

    public String getSysTags() {
        return sysTags;
    }

    public void setSysTags(final String sysTags) {
        this.sysTags = sysTags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getGroupList() {
        return groupList;
    }

    public void setGroupList(final String groupList) {
        this.groupList = groupList;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(final String priority) {
        this.priority = priority;
    }

    public String getDeliveryPlan() {
        return deliveryPlan;
    }

    public void setDeliveryPlan(final String deliveryPlan) {
        this.deliveryPlan = deliveryPlan;
    }

    public String getSysModCount() {
        return sysModCount;
    }

    public void setSysModCount(final String sysModCount) {
        this.sysModCount = sysModCount;
    }

    public String getWorkNotesList() {
        return workNotesList;
    }

    public void setWorkNotesList(final String workNotesList) {
        this.workNotesList = workNotesList;
    }

    public String getBusinessService() {
        return businessService;
    }

    public void setBusinessService(final String businessService) {
        this.businessService = businessService;
    }

    public String getFollowUp() {
        return followUp;
    }

    public void setFollowUp(final String followUp) {
        this.followUp = followUp;
    }

    public String getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(final String closedAt) {
        this.closedAt = closedAt;
    }

    public String getSlaDue() {
        return slaDue;
    }

    public void setSlaDue(final String slaDue) {
        this.slaDue = slaDue;
    }

    public String getDeliveryTask() {
        return deliveryTask;
    }

    public void setDeliveryTask(final String deliveryTask) {
        this.deliveryTask = deliveryTask;
    }

    public String getSysUpdatedOn() {
        return sysUpdatedOn;
    }

    public void setSysUpdatedOn(final String sysUpdatedOn) {
        this.sysUpdatedOn = sysUpdatedOn;
    }

    public String getWorkEnd() {
        return workEnd;
    }

    public void setWorkEnd(final String workEnd) {
        this.workEnd = workEnd;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(final String closedBy) {
        this.closedBy = closedBy;
    }

    public String getWorkStart() {
        return workStart;
    }

    @JsonProperty("work_start")
    public void setWorkStart(final String workStart) {
        this.workStart = workStart;
    }

    public String getCalendarStc() {
        return calendarStc;
    }

    public void setCalendarStc(final String calendarStc) {
        this.calendarStc = calendarStc;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getBusinessDuration() {
        return businessDuration;
    }

    public void setBusinessDuration(final String businessDuration) {
        this.businessDuration = businessDuration;
    }

    public String getIncidentState() {
        return incidentState;
    }

    public void setIncidentState(final String incidentState) {
        this.incidentState = incidentState;
    }

    public String getActivityDue() {
        return activityDue;
    }

    public void setActivityDue(final String activityDue) {
        this.activityDue = activityDue;
    }

    public String getCorrelationDisplay() {
        return correlationDisplay;
    }

    public void setCorrelationDisplay(final String correlationDisplay) {
        this.correlationDisplay = correlationDisplay;
    }

    public String getActive() {
        return active;
    }

    public void setActive(final String active) {
        this.active = active;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(final String dueDate) {
        this.dueDate = dueDate;
    }

    public String getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(final String knowledge) {
        this.knowledge = knowledge;
    }

    public String getMadeSla() {
        return madeSla;
    }

    public void setMadeSla(final String madeSla) {
        this.madeSla = madeSla;
    }

    public String getCommentsAndWorkNotes() {
        return commentsAndWorkNotes;
    }

    public void setCommentsAndWorkNotes(final String commentsAndWorkNotes) {
        this.commentsAndWorkNotes = commentsAndWorkNotes;
    }

    public String getParentIncident() {
        return parentIncident;
    }

    public void setParentIncident(final String parentIncident) {
        this.parentIncident = parentIncident;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(final String userInput) {
        this.userInput = userInput;
    }

    public String getSysCreatedOn() {
        return sysCreatedOn;
    }

    public void setSysCreatedOn(final String sysCreatedOn) {
        this.sysCreatedOn = sysCreatedOn;
    }

    public String getApprovalSet() {
        return approvalSet;
    }

    public void setApprovalSet(final String approvalSet) {
        this.approvalSet = approvalSet;
    }

    public String getReassignmentCount() {
        return reassignmentCount;
    }

    public void setReassignmentCount(final String reassignmentCount) {
        this.reassignmentCount = reassignmentCount;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(final String rfc) {
        this.rfc = rfc;
    }

    public String getChildIncidents() {
        return childIncidents;
    }

    public void setChildIncidents(final String childIncidents) {
        this.childIncidents = childIncidents;
    }

    public String getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(final String openedAt) {
        this.openedAt = openedAt;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(final String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(final String order) {
        this.order = order;
    }

    public String getSysUpdatedBy() {
        return sysUpdatedBy;
    }

    public void setSysUpdatedBy(final String sysUpdatedBy) {
        this.sysUpdatedBy = sysUpdatedBy;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(final String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getNotify() {
        return notify;
    }

    public void setNotify(final String notify) {
        this.notify = notify;
    }

    public String getUponReject() {
        return uponReject;
    }

    public void setUponReject(final String uponReject) {
        this.uponReject = uponReject;
    }

    public String getApprovalHistory() {
        return approvalHistory;
    }

    public void setApprovalHistory(final String approvalHistory) {
        this.approvalHistory = approvalHistory;
    }

    public String getProblemId() {
        return problemId;
    }

    public void setProblemId(final String problemId) {
        this.problemId = problemId;
    }

    public String getWorkNotes() {
        return workNotes;
    }

    public void setWorkNotes(final String workNotes) {
        this.workNotes = workNotes;
    }

    public String getCalendarDuration() {
        return calendarDuration;
    }

    public void setCalendarDuration(final String calendarDuration) {
        this.calendarDuration = calendarDuration;
    }

    public String getCloseCode() {
        return closeCode;
    }

    public void setCloseCode(final String closeCode) {
        this.closeCode = closeCode;
    }

    @Override
    public String getSysId() {
        return sysId;
    }

    @Override
    public void setSysId(final String sysId) {
        this.sysId = sysId;
    }

    public String getApproval() {
        return approval;
    }

    public void setApproval(final String approval) {
        this.approval = approval;
    }

    public String getCausedBy() {
        return causedBy;
    }

    public void setCausedBy(final String causedBy) {
        this.causedBy = causedBy;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(final String severity) {
        this.severity = severity;
    }

    public String getSysCreatedBy() {
        return sysCreatedBy;
    }

    public void setSysCreatedBy(final String sysCreatedBy) {
        this.sysCreatedBy = sysCreatedBy;
    }

    public String getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(final String resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(final String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getBusinessStc() {
        return businessStc;
    }

    public void setBusinessStc(final String businessStc) {
        this.businessStc = businessStc;
    }

    public String getWfActivity() {
        return wfActivity;
    }

    public void setWfActivity(final String wfActivity) {
        this.wfActivity = wfActivity;
    }

    public String getSysDomainPath() {
        return sysDomainPath;
    }

    public void setSysDomainPath(final String sysDomainPath) {
        this.sysDomainPath = sysDomainPath;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(final String subcategory) {
        this.subcategory = subcategory;
    }

    public String getRejectionGoto() {
        return rejectionGoto;
    }

    public void setRejectionGoto(final String rejectionGoto) {
        this.rejectionGoto = rejectionGoto;
    }

    public String getSysClassName() {
        return sysClassName;
    }

    public void setSysClassName(final String sysClassName) {
        this.sysClassName = sysClassName;
    }

    public String getWatchList() {
        return watchList;
    }

    public void setWatchList(final String watchList) {
        this.watchList = watchList;
    }

    public String getTimeWorked() {
        return timeWorked;
    }

    public void setTimeWorked(final String timeWorked) {
        this.timeWorked = timeWorked;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(final String contactType) {
        this.contactType = contactType;
    }

    public String getEscalation() {
        return escalation;
    }

    public void setEscalation(final String escalation) {
        this.escalation = escalation;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public String getCalendarIntegration() {
        return calendarIntegration;
    }

    public void setCalendarIntegration(final String calendarIntegration) {
        this.calendarIntegration = calendarIntegration;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(final String userPassword) {
        this.userPassword = userPassword;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(final String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(final String building) {
        this.building = building;
    }

    public String getWebServiceAccessOnly() {
        return webServiceAccessOnly;
    }

    public void setWebServiceAccessOnly(final String webServiceAccessOnly) {
        this.webServiceAccessOnly = webServiceAccessOnly;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(final String notification) {
        this.notification = notification;
    }

    public String getEnableMultifactorAuthn() {
        return enableMultifactorAuthn;
    }

    public void setEnableMultifactorAuthn(final String enableMultifactorAuthn) {
        this.enableMultifactorAuthn = enableMultifactorAuthn;
    }

    public String getVip() {
        return vip;
    }

    public void setVip(final String vip) {
        this.vip = vip;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(final String zip) {
        this.zip = zip;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(final String homePhone) {
        this.homePhone = homePhone;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(final String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(final String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(final String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getPasswordNeedsReset() {
        return passwordNeedsReset;
    }

    public void setPasswordNeedsReset(final String passwordNeedsReset) {
        this.passwordNeedsReset = passwordNeedsReset;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(final String gender) {
        this.gender = gender;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(final String failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(final String roles) {
        this.roles = roles;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getInternalIntegrationUser() {
        return internalIntegrationUser;
    }

    public void setInternalIntegrationUser(final String internalIntegrationUser) {
        this.internalIntegrationUser = internalIntegrationUser;
    }

    public String getLdapServer() {
        return ldapServer;
    }

    public void setLdapServer(final String ldapServer) {
        this.ldapServer = ldapServer;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(final String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(final String street) {
        this.street = street;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(final String introduction) {
        this.introduction = introduction;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(final String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getLockedOut() {
        return lockedOut;
    }

    public void setLockedOut(final String lockedOut) {
        this.lockedOut = lockedOut;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(final String photo) {
        this.photo = photo;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(final String middleName) {
        this.middleName = middleName;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(final String timeZone) {
        this.timeZone = timeZone;
    }

    public SNComplex getSchedule() {
        return schedule;
    }

    public void setSchedule(final SNComplex schedule) {
        this.schedule = schedule;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(final String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public SNComplex getDefaultPerspective() {
        return defaultPerspective;
    }

    public void setDefaultPerspective(final SNComplex defaultPerspective) {
        this.defaultPerspective = defaultPerspective;
    }

    public SNComplex getParent() {
        return parent;
    }

    public void setParent(final SNComplex parent) {
        this.parent = parent;
    }

    public SNComplex getCostCenter() {
        return costCenter;
    }

    public SNComplex getDepartment() {
        return department;
    }

    public void setDepartment(final SNComplex department) {
        this.department = department;
    }

    public void setCostCenter(final SNComplex costCenter) {
        this.costCenter = costCenter;
    }

    public SNComplex getManager() {
        return manager;
    }

    public void setManager(final SNComplex manager) {
        this.manager = manager;
    }

    public SNComplex getCmdbCi() {
        return cmdbCi;
    }

    public void setCmdbCi(final SNComplex cmdbCi) {
        this.cmdbCi = cmdbCi;
    }

    public SNComplex getOpenedBy() {
        return openedBy;
    }

    public void setOpenedBy(final SNComplex openedBy) {
        this.openedBy = openedBy;
    }

    public SNComplex getLocation() {
        return location;
    }

    public void setLocation(final SNComplex location) {
        this.location = location;
    }

    public SNComplex getSysDomain() {
        return sysDomain;
    }

    public void setSysDomain(final SNComplex sysDomain) {
        this.sysDomain = sysDomain;
    }

    public SNComplex getCompany() {
        return company;
    }

    public void setCompany(final SNComplex company) {
        this.company = company;
    }

    public SNComplex getAssignmentGroup() {
        return assignmentGroup;
    }

    public void setAssignmentGroup(final SNComplex assignmentGroup) {
        this.assignmentGroup = assignmentGroup;
    }

    public SNComplex getCallerId() {
        return callerId;
    }

    public void setCallerId(final SNComplex callerId) {
        this.callerId = callerId;
    }

    @JsonIgnore
    @Override
    public Set<Attribute> toAttributes() throws IllegalArgumentException, IllegalAccessException {
        Set<Attribute> attrs = new HashSet<>();

        Field[] fields = Resource.class.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(JsonIgnore.class) && field.isAnnotationPresent(JsonProperty.class)) {
                field.setAccessible(true);

                String newName = SNUtils.fromUnderscoredToCamelCase(field.getName());
                Object value = field.get(this);
                if (value != null) {
                    switch (newName) {
                        case "manager":
                            value = SNComplex.class.cast(value).getValue();
                            break;
                        case "department":
                            value = SNComplex.class.cast(value).getValue();
                            break;
                        case "cost_center":
                            value = SNComplex.class.cast(value).getValue();
                            break;
                        case "opened_by":
                            value = SNComplex.class.cast(value).getValue();
                            break;
                        case "sys_domain":
                            value = SNComplex.class.cast(value).getValue();
                            break;
                        case "location":
                            value = SNComplex.class.cast(value).getValue();
                            break;
                        case "company":
                            value = SNComplex.class.cast(value).getValue();
                            break;
                        case "assignment_group":
                            value = SNComplex.class.cast(value).getValue();
                            break;
                        case "cmdb_ci":
                            value = SNComplex.class.cast(value).getValue();
                            break;
                        case "default_perspective":
                            value = SNComplex.class.cast(value).getValue();
                            break;
                        case "parent":
                            value = SNComplex.class.cast(value).getValue();
                            break;
                    }
                }
                attrs.add(SNAttributes.buildAttributeFromClassField(field,
                        SNUtils.fromCamelCaseToUnderscored(field.getName()),
                        value).build());
            }
        }

        return attrs;
    }

    @JsonIgnore
    @Override
    public void fromAttributes(final Set<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            if (!CollectionUtil.isEmpty(attribute.getValue())) {
                List<Object> values = attribute.getValue();
                String localName = attribute.getName();

                if (localName.contains("__")) {
                    LOG.ok("Skipping attribute {0}, while building Resource object", localName);
                    continue;
                }

                try {
                    String newName = SNUtils.fromUnderscoredToCamelCase(localName);
                    Field field = Resource.class.getDeclaredField(newName);
                    field.setAccessible(true);

                    switch (newName) {
                        case "manager":
                            manager = new SNComplex(String.class.cast(values.get(0)));
                            break;
                        case "department":
                            department = new SNComplex(String.class.cast(values.get(0)));
                            break;
                        case "cost_center":
                            costCenter = new SNComplex(String.class.cast(values.get(0)));
                            break;
                        case "opened_by":
                            openedBy = new SNComplex(String.class.cast(values.get(0)));
                            break;
                        case "sys_domain":
                            sysDomain = new SNComplex(String.class.cast(values.get(0)));
                            break;
                        case "location":
                            location = new SNComplex(String.class.cast(values.get(0)));
                            break;
                        case "company":
                            company = new SNComplex(String.class.cast(values.get(0)));
                            break;
                        case "assignment_group":
                            assignmentGroup = new SNComplex(String.class.cast(values.get(0)));
                            break;
                        case "cmdb_ci":
                            cmdbCi = new SNComplex(String.class.cast(values.get(0)));
                            break;
                        case "default_perspective":
                            defaultPerspective = new SNComplex(String.class.cast(values.get(0)));
                            break;
                        case "schedule":
                            schedule = new SNComplex(String.class.cast(values.get(0)));
                            break;
                        case "parent":
                            parent = new SNComplex(String.class.cast(values.get(0)));
                            break;
                        default:
                            field.set(this, values.get(0) instanceof String ? String.class.cast(values.get(0))
                                    : (values.get(0) instanceof Boolean ? Boolean.class.cast(values.get(0)) : null));
                    }
                } catch (NoSuchFieldException | SecurityException ex) {
                    LOG.error(ex, "Field {0} not found", localName);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    LOG.error(ex, "While setting value to field");
                }
            }
        }
    }

    @JsonIgnore
    public static Map<String, String> asMapAttributeField() {
        Map<String, String> map = new HashMap<>();
        Field[] fields = Resource.class.getDeclaredFields();

        for (Field field : fields) {
            if (!field.isAnnotationPresent(JsonIgnore.class) && field.isAnnotationPresent(JsonProperty.class)) {
                map.put(field.getAnnotation(JsonProperty.class).value(), field.getName());
            }
        }

        return Collections.unmodifiableMap(map);
    }

    @Override
    public String toString() {
        return "User{" + "uponApproval=" + uponApproval + ", location=" + location + ", expectedStart=" + expectedStart
                + ", reopenCount=" + reopenCount + ", closeNotes=" + closeNotes + ", additionalAssigneeList="
                + additionalAssigneeList + ", impact=" + impact + ", urgency=" + urgency + ", correlationId="
                + correlationId + ", sysTags=" + sysTags + ", description=" + description + ", groupList=" + groupList
                + ", priority=" + priority + ", deliveryPlan=" + deliveryPlan + ", sysModCount=" + sysModCount
                + ", workNotesList=" + workNotesList + ", businessService=" + businessService + ", followUp=" + followUp
                + ", closedAt=" + closedAt + ", slaDue=" + slaDue + ", deliveryTask=" + deliveryTask + ", sysUpdatedOn="
                + sysUpdatedOn + ", parent=" + parent + ", workEnd=" + workEnd + ", number=" + number + ", closedBy="
                + closedBy + ", workStart=" + workStart + ", calendarStc=" + calendarStc + ", category=" + category
                + ", businessDuration=" + businessDuration + ", incidentState=" + incidentState + ", activityDue="
                + activityDue + ", correlationDisplay=" + correlationDisplay + ", company=" + company + ", active="
                + active + ", dueDate=" + dueDate + ", callerId=" + callerId + ", knowledge=" + knowledge + ", madeSla="
                + madeSla + ", commentsAndWorkNotes=" + commentsAndWorkNotes + ", parentIncident=" + parentIncident
                + ", state=" + state + ", userInput=" + userInput + ", sysCreatedOn=" + sysCreatedOn + ", approvalSet="
                + approvalSet + ", reassignmentCount=" + reassignmentCount + ", rfc=" + rfc + ", childIncidents="
                + childIncidents + ", openedAt=" + openedAt + ", shortDescription=" + shortDescription + ", order="
                + order + ", sysUpdatedBy=" + sysUpdatedBy + ", resolvedBy=" + resolvedBy + ", notify=" + notify
                + ", uponReject=" + uponReject + ", approvalHistory=" + approvalHistory + ", problemId=" + problemId
                + ", workNotes=" + workNotes + ", calendarDuration=" + calendarDuration + ", closeCode=" + closeCode
                + ", sysId=" + sysId + ", approval=" + approval + ", causedBy=" + causedBy + ", severity=" + severity
                + ", sysCreatedBy=" + sysCreatedBy + ", resolvedAt=" + resolvedAt + ", assignedTo=" + assignedTo
                + ", businessStc=" + businessStc + ", wfActivity=" + wfActivity + ", sysDomainPath=" + sysDomainPath
                + ", subcategory=" + subcategory + ", rejectionGoto=" + rejectionGoto + ", sysClassName=" + sysClassName
                + ", watchList=" + watchList + ", timeWorked=" + timeWorked + ", contactType=" + contactType
                + ", escalation=" + escalation + ", comments=" + comments + ", openedBy=" + openedBy + ", sysDomain="
                + sysDomain + ", assignmentGroup=" + assignmentGroup + ", cmdbCi=" + cmdbCi + ", calendarIntegration="
                + calendarIntegration + ", country=" + country + ", userPassword=" + userPassword + ", lastLoginTime="
                + lastLoginTime + ", source=" + source + ", building=" + building + ", webServiceAccessOnly="
                + webServiceAccessOnly + ", notification=" + notification + ", enableMultifactorAuthn="
                + enableMultifactorAuthn + ", vip=" + vip + ", zip=" + zip + ", homePhone=" + homePhone
                + ", timeFormat=" + timeFormat + ", lastLogin=" + lastLogin + ", defaultPerspective="
                + defaultPerspective + ", costCenter=" + costCenter + ", phone=" + phone + ", name=" + name
                + ", employeeNumber=" + employeeNumber + ", passwordNeedsReset=" + passwordNeedsReset + ", gender="
                + gender + ", city=" + city + ", failedAttempts=" + failedAttempts + ", userName=" + userName
                + ", roles=" + roles + ", title=" + title + ", internalIntegrationUser=" + internalIntegrationUser
                + ", ldapServer=" + ldapServer + ", mobilePhone=" + mobilePhone + ", street=" + street + ", department="
                + department + ", firstName=" + firstName + ", email=" + email + ", introduction=" + introduction
                + ", preferredLanguage=" + preferredLanguage + ", manager=" + manager + ", lockedOut=" + lockedOut
                + ", lastName=" + lastName + ", photo=" + photo + ", middleName=" + middleName + ", timeZone="
                + timeZone + ", schedule=" + schedule + ", dateFormat=" + dateFormat + '}';
    }

}
