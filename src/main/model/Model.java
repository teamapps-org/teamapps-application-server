import org.teamapps.universaldb.schema.*;

import static org.teamapps.universaldb.schema.TableOption.*;

public class Model implements SchemaInfoProvider {


	@Override
	public Schema getSchema() {
		Schema schema = Schema.create("org.teamapps.model");
		schema.setSchemaName("ControlCenterSchema");
		Database db = schema.addDatabase("controlCenter");


		Table user = db.addTable("user", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);
		Table userAccessToken = db.addTable("userAccessToken", KEEP_DELETED, TRACK_CREATION);
		Table organizationUnit = db.addTable("organizationUnit", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);
		Table organizationUnitType = db.addTable("organizationUnitType", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);
		Table organizationField = db.addTable("organizationField", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);
		Table userContainer = db.addTable("userContainer", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);

		Table role = db.addTable("role", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);
		Table userRoleAssignment = db.addTable("userRoleAssignment", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);
		Table rolePrivilegeAssignment = db.addTable("rolePrivilegeAssignment", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);
		Table roleApplicationRoleAssignment = db.addTable("roleApplicationRoleAssignment", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);


		Table systemSettings = db.addTable("systemSettings", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);
		Table application = db.addTable("application", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);
		Table applicationPerspective = db.addTable("applicationPerspective", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);
		Table applicationPrivilegeGroup = db.addTable("applicationPrivilegeGroup", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);
		Table applicationPrivilege = db.addTable("applicationPrivilege", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);
		Table applicationVersion = db.addTable("applicationVersion", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);
		Table applicationVersionData = db.addTable("applicationVersionData", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);
		Table applicationActivity = db.addTable("applicationActivity", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION);
		Table managedApplication = db.addTable("managedApplication", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);
		Table managedApplicationPerspective = db.addTable("managedApplicationPerspective", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);

		Table managedApplicationGroup = db.addTable("managedApplicationGroup", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);

		Table localizationKey = db.addTable("localizationKey", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);
		Table localizationValue = db.addTable("localizationValue", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);
		Table localizationTopic = db.addTable("localizationTopic", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);

		//workspace model:
		Table group = db.addTable("group", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);
		Table groupMembershipDefinition = db.addTable("groupMembershipDefinition", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);
		Table groupFolder = db.addTable("groupFolder", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);
		Table userGroupMembership = db.addTable("userGroupMembership");
		Table applicationFolder = db.addTable("applicationFolder", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);
		Table message = db.addTable("message", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);
		Table appointment = db.addTable("appointment", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);
		Table appointmentSeries = db.addTable("appointmentSeries", KEEP_DELETED, TRACK_CREATION, TRACK_MODIFICATION);


		systemSettings
				.addText("allowedBaseLanguages")
		;
		application
				.addText("name")
				.addText("icon")
				.addText("titleKey")
				.addText("descriptionKey")
				.addBoolean("unmanagedApplication")
				.addReference("perspectives", applicationPerspective, true, "application", true)
				.addReference("privilegeGroups", applicationPrivilegeGroup, true, "application")
				.addReference("versions", applicationVersion, true, "application")
				.addReference("installedVersion", applicationVersion, false)
				.addReference("installedAsMainApplication", managedApplication, true, "mainApplication")
		;

		applicationPerspective
				.addReference("application", application, false, "perspectives")
				.addText("name")
				.addText("icon")
				.addText("titleKey")
				.addText("descriptionKey")
				.addReference("managedPerspectives", managedApplicationPerspective, true, "applicationPerspective", true)
		;

		applicationPrivilegeGroup
				.addReference("application", application, false, "privilegeGroups")
				.addEnum("applicationPrivilegeGroupType", "simplePrivilege", "simpleOrganizationalPrivilege", "simpleCustomObjectPrivilege", "standardPrivilegeGroup", "organizationalPrivilegeGroup", "customObjectPrivilegeGroup")
				.addText("name")
				.addText("icon")
				.addText("titleKey")
				.addText("descriptionKey")
				.addReference("privileges", applicationPrivilege, true, "privilegeGroup")
		;

		applicationPrivilege
				.addReference("privilegeGroup", applicationPrivilegeGroup, false, "privileges")
				.addText("name")
				.addText("icon")
				.addText("titleKey")
		;

		applicationVersion
				.addReference("application", application, false, "versions")
				.addFile("binary")
				.addText("binaryHash")
				.addText("version")
				.addText("releaseNotes")
				.addReference("dataModelData", applicationVersionData, false)
				.addReference("localizationData", applicationVersionData, false)
				.addReference("privilegeData", applicationVersionData, false)
				.addReference("perspectiveData", applicationVersionData, false)
		;

		applicationVersionData
				.addText("data")
				.addInteger("dataRows")
				.addText("dataAdded")
				.addInteger("dataAddedRows")
				.addText("dataRemoved")
				.addInteger("dataRemovedRows")
		;

		applicationActivity
				.addReference("managedApplication", managedApplication, false)
				.addReference("managedPerspective", managedApplicationPerspective, false)
				.addReference("application", application, false)
				.addBoolean("error")
				.addText("activity")
				.addText("details")
		;

		managedApplication
				.addReference("mainApplication", application, false, "installedAsMainApplication")
				.addReference("organizationField", organizationField, false)
				.addText("icon")
				.addText("titleKey")
				.addText("descriptionKey")
				.addBoolean("singleApplication")
				.addReference("perspectives", managedApplicationPerspective, true, "managedApplication", true)
				.addInteger("listingPosition")
				.addReference("applicationGroup", managedApplicationGroup, false, "applications")
				.addBoolean("hidden")
		;

		managedApplicationPerspective
				.addReference("managedApplication", managedApplication, false, "perspectives")
				.addReference("applicationPerspective", applicationPerspective, false, "managedPerspectives")
				.addText("iconOverride")
				.addText("titleKeyOverride")
				.addText("descriptionKeyOverride")
				.addInteger("listingPosition")
		;

		managedApplicationGroup
				.addText("icon")
				.addText("titleKey") //.addTranslatableText("title")
				.addReference("applications", managedApplication, true, "applicationGroup")
				.addInteger("listingPosition")
		;

		localizationKey
				.addReference("application", application, false)
				.addText("key")
				.addBoolean("used")
				.addEnum("localizationKeyFormat", "singleLine", "multiLine", "formatted")
				.addEnum("localizationKeyType", "applicationResourceKey", "dictionaryKey", "reportingKey", "systemKey")
				.addText("comments")
				.addReference("localizationValues", localizationValue, true, "localizationKey")
				.addReference("topics", localizationTopic, true, "keys")
		;

		localizationValue
				.addReference("localizationKey", localizationKey, false, "localizationValues")
				.addText("language")
				.addText("original")
				.addText("machineTranslation")
				.addText("translation")
				.addText("adminLocalOverride") //override only this translation
				.addText("adminKeyOverride") //override all translations
				.addText("currentDisplayValue")
				.addText("notes")
				.addEnum("machineTranslationState", "translationRequested", "ok", "failed", "notNecessary")
				.addEnum("translationState", "translationRequested", "ok", "unclear", "notNecessary")
				.addEnum("translationVerificationState", "notYetTranslated", "verificationRequested", "ok", "correctionsRequired", "notNecessary")
		;

		localizationTopic
				.addReference("keys", localizationKey, true, "topics")
				.addReference("application", application, false)
				.addText("icon")
				.addText("title")
				.addFile("screenshot")
		;

		user
				.addText("firstName")
				.addText("lastName")
				.addFile("profilePicture")
				.addFile("profilePictureLarge")
				.addText("languages")
				.addText("email")
				.addText("mobile")
				.addText("login")
				.addText("password")
				.addText("theme")
				.addEnum("userAccountStatus", "active", "inactive", "superAdmin")
				.addReference("container", userContainer, false, "users")
				.addReference("accessTokens", userAccessToken, true, "user")
				.addReference("roleAssignments", userRoleAssignment, true, "user")
				.addReference("allGroupMemberships", userGroupMembership, true, "user")
				.addReference("privateMessages", message, true, "privateRecipients") //private message entry instead!?
		;

		userAccessToken
				.addReference("user", user, false, "accessTokens")
				.addText("userAgentOnCreation")
				.addText("userAgentLastUsed")
				.addTimestamp("lastUsed")
				.addBoolean("valid")
				.addText("secureToken")
		;

		organizationUnit
				.addTranslatableText("name")
				.addReference("parent", organizationUnit, false, "children")
				.addReference("children", organizationUnit, true, "parent")
				.addReference("type", organizationUnitType, false)
				.addText("icon")
				.addReference("userContainer", userContainer, false, "organizationUnit")
		;

		organizationUnitType
				.addTranslatableText("name")
				.addTranslatableText("abbreviation")
				.addText("icon")
				.addBoolean("translateOrganizationUnits")
				.addBoolean("allowUserContainer")
				.addReference("defaultChildType", organizationUnitType, false)
				.addReference("possibleChildrenTypes", organizationUnitType, true)
		;

		organizationField
				.addText("name")
				.addTranslatableText("title")
				.addText("icon")
		;


		userContainer
				.addReference("organizationUnit", organizationUnit, false, "userContainer")
				.addReference("users", user, true, "container")
		;

		role
				.addTranslatableText("title")
				.addText("icon")
				.addReference("parent", role, false, "children")
				.addReference("children", role, true, "parent")
				.addReference("allowedOrganizationUnitTypes", organizationUnitType, true)
				.addReference("organizationField", organizationField, false)
				.addReference("generalizationRoles", role, true, "specializationRoles")
				.addReference("specializationRoles", role, true, "generalizationRoles")
				.addReference("privilegesReceivingRoles", role, true, "privilegesSendingRoles")
				.addReference("privilegesSendingRoles", role, true, "privilegesReceivingRoles")
				.addBoolean("noDirectMemberships")
				.addReference("userRoleAssignments", userRoleAssignment, true, "role")
				.addReference("privilegeAssignments", rolePrivilegeAssignment, true, "role")
				.addReference("applicationRoleAssignments", roleApplicationRoleAssignment, true, "role")
		;

		userRoleAssignment
				.addReference("user", user, false, "roleAssignments")
				.addReference("role", role, false, "userRoleAssignments")
				.addReference("organizationUnit", organizationUnit, false)
				.addTimestamp("lastVerified")
				.addReference("lastVerifiedBy", user, false)
		;

		rolePrivilegeAssignment
				.addReference("role", role, false, "privilegeAssignments")
				.addReference("application", application, false)
				.addText("privilegeGroup")
				.addText("privileges")
				.addText("privilegeObjects")
				.addBoolean("privilegeObjectInheritance")
				.addReference("organizationFieldFilter", organizationField, false)
				.addReference("fixedOrganizationRoot", organizationUnit, false)
				.addReference("organizationUnitTypeFilter", organizationUnitType, true)
		;

		roleApplicationRoleAssignment
				.addReference("role", role, false, "applicationRoleAssignments")
				.addReference("application", application, false)
				.addText("applicationRoleName")
				.addReference("organizationFieldFilter", organizationField, false)
				.addReference("fixedOrganizationRoot", organizationUnit, false)
				.addReference("organizationUnitTypeFilter", organizationUnitType, true)
		;


		group
				.addEnum("groupType", "messageGroup", "workGroup")
				.addText("name") //addTranslatableText
				.addText("description") //addTranslatableText
				.addText("language")
				.addReference("organizationField", organizationField, false)
				.addReference("organizationUnit", organizationUnit, false)
				.addReference("owner", user, false)
				.addReference("moderators", user, true)
				.addReference("mentors", user, true)
				.addBoolean("valid")
				.addReference("membershipDefinitions", groupMembershipDefinition, true, "group")
				.addReference("registrationAllowedForDefinitions", groupMembershipDefinition, true)
				.addReference("userMemberships", userGroupMembership, true, "group")
				.addTimestamp("lastMessageDate")
				.addReference("groupFolders", groupFolder, true, "group")
		;

		groupMembershipDefinition
				.addEnum("groupMemberType", "user", "group", "roleMember", "userContainer")
				.addReference("user", user, false)
				.addReference("group", group, false)
				.addReference("role", role, false)
				.addReference("organizationUnit", organizationUnit, false)
				.addReference("organizationUnitTypesFilter", organizationUnitType, true)
		;

		groupFolder
				.addReference("group", group, false, "groupFolders")
				.addText("name")
				.addBoolean("moderatorReadAccess")
				.addBoolean("moderatorWriteAccess")
				.addText("subjectTags") //message/chat subjects, file tags,
				.addEnum("groupFolderType", "chat", "messages", "files", "calendar")
				.addReference("messages", message, true, "groupFolder")
			/*
				Automatic folders:
					-Conference recordings (if calendar folder exists)
					-All files (from chats, messages, file folders)
			 */
		;

		applicationFolder
				.addText("name")
				.addReference("application", application, false)
				.addEnum("applicationFolderType", "chat", "messages", "files", "calendar")
				.addReference("parent", applicationFolder, false, "children")
				.addReference("children", applicationFolder, true, "parent")
				.addReference("messages", message, true, "applicationFolder")

		;


		userGroupMembership
				.addReference("group", group, false, "userMemberships")
				.addReference("user", user, false, "allGroupMemberships")
				.addEnum("groupMembershipRole", "participant", "moderator", "mentor", "owner")
				.addBoolean("membershipRejected")
				.addEnum("groupMembershipNotificationSetting", "standard", "silent");


		message
				.addEnum("messageRecipientType", "private", "groupFolder", "applicationFolder") //todo message group as recipient?
				.addReference("privateRecipients", user, true, "privateMessages")
				.addReference("groupFolder", groupFolder, false, "messages")
				.addReference("applicationFolder", applicationFolder, false, "messages")
				.addText("subject") //todo translatable text?
				.addText("message") //todo translatable text?
				.addEnum("messageType", "message", "chatMessage", "appointment", "appointmentSeries", "file")
				.addFile("file")
				.addReference("appointment", appointment, false, "message")
				.addReference("appointmentSeries", appointmentSeries, false, "message")
		;

		appointment
				.addReference("message", message, false, "appointment")
				.addReference("appointmentSeries", appointmentSeries, false, "appointments")
				.addBoolean("webConference") //conference object -> recording
				.addDateTime("startDateTime")
				.addDateTime("endDateTime")
				.addBoolean("fullDayEvent")
		;

		appointmentSeries
				.addReference("message", message, false, "appointmentSeries")
				.addReference("appointments", appointment, true, "appointmentSeries")
				.addEnum("appointmentSeriesType", "daily", "weekly", "monthly", "yearly")
				.addInteger("repeatEvery")
				.addDateTime("startDateTime")
				.addDateTime("endDateTime")
				.addBoolean("fullDayEvent")
				.addLocalDate("seriesEndDate");


		return schema;
	}
}
