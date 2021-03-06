package edu.yale.library.ladybird.persistence.dao;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import edu.yale.library.ladybird.entity.Project;
import edu.yale.library.ladybird.entity.User;
import edu.yale.library.ladybird.persistence.dao.hibernate.CollectionHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.EventTypeHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.HydraHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.HydraPublishHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ImportFileHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ImportJobHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ImportJobNotificationsHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ImportSourceHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ImportSourceDataHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.FieldDefinitionHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.FieldMarcMappingHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.JobRequestHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ObjectAcidVersionHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ObjectEventHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ObjectFileHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ObjectHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ObjectStringVersionHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ObjectVersionHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ProjectHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ProjectTemplateAcidHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ProjectTemplateHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ProjectTemplateStringsHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.UserHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.UserPreferencesHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.UserEventHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.UserProjectFieldExportOptionsHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.UserProjectHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.RolesHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.PermissionsHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.RolesPermissionsHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.AuthorityControlHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.AuthorityControlVersionHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.AccessconditionObjectHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.AccessconditionProjectHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.AccessconditionTargetHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.AccessconditionTypeHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.SettingsHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ObjectAcidHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ObjectStringHibernateDAO;


public class TestDaoHibernateModule extends AbstractModule {
    @Override
    protected void configure() {
        TypeLiteral<GenericDAO<User, Integer>> userDaoType
                = new TypeLiteral<GenericDAO<User, Integer>>() { };

        TypeLiteral<GenericDAO<Project, Integer>> projectDaoType
                = new TypeLiteral<GenericDAO<Project, Integer>>() { };

        bind(userDaoType).to(UserDAO.class);
        bind(UserDAO.class).to(UserHibernateDAO.class);
        bind(UserPreferencesDAO.class).to(UserPreferencesHibernateDAO.class);
        bind(JobRequestDAO.class).to(JobRequestHibernateDAO.class);
        bind(ImportFileDAO.class).to(ImportFileHibernateDAO.class);
        bind(ImportSourceDAO.class).to(ImportSourceHibernateDAO.class);
        bind(ImportSourceDataDAO.class).to(ImportSourceDataHibernateDAO.class);
        bind(ProjectDAO.class).to(ProjectHibernateDAO.class);
        bind(CollectionDAO.class).to(CollectionHibernateDAO.class);
        bind(FieldDefinitionDAO.class).to(FieldDefinitionHibernateDAO.class);
        bind(FieldMarcMappingDAO.class).to(FieldMarcMappingHibernateDAO.class);
        bind(ObjectDAO.class).to(ObjectHibernateDAO.class);
        bind(ObjectFileDAO.class).to(ObjectFileHibernateDAO.class);
        bind(UserEventDAO.class).to(UserEventHibernateDAO.class);
        bind(UserProjectDAO.class).to(UserProjectHibernateDAO.class);
        bind(RolesDAO.class).to(RolesHibernateDAO.class);
        bind(PermissionsDAO.class).to(PermissionsHibernateDAO.class);
        bind(RolesPermissionsDAO.class).to(RolesPermissionsHibernateDAO.class);
        bind(AuthorityControlDAO.class).to(AuthorityControlHibernateDAO.class);
        bind(AuthorityControlVersionDAO.class).to(AuthorityControlVersionHibernateDAO.class);
        bind(AccessconditionTypeDAO.class).to(AccessconditionTypeHibernateDAO.class);
        bind(AccessconditionTargetDAO.class).to(AccessconditionTargetHibernateDAO.class);
        bind(AccessconditionProjectDAO.class).to(AccessconditionProjectHibernateDAO.class);
        bind(AccessconditionObjectDAO.class).to(AccessconditionObjectHibernateDAO.class);
        bind(SettingsDAO.class).to(SettingsHibernateDAO.class);
        bind(ObjectAcidDAO.class).to(ObjectAcidHibernateDAO.class);
        bind(ObjectStringDAO.class).to(ObjectStringHibernateDAO.class);
        bind(HydraDAO.class).to(HydraHibernateDAO.class);
        bind(HydraPublishDAO.class).to(HydraPublishHibernateDAO.class);
        bind(ProjectTemplateDAO.class).to(ProjectTemplateHibernateDAO.class);
        bind(ProjectTemplateStringsDAO.class).to(ProjectTemplateStringsHibernateDAO.class);
        bind(ProjectTemplateAcidDAO.class).to(ProjectTemplateAcidHibernateDAO.class);
        bind(ImportJobNotificationsDAO.class).to(ImportJobNotificationsHibernateDAO.class);
        bind(ImportJobDAO.class).to(ImportJobHibernateDAO.class);
        bind(UserProjectFieldExportOptionsDAO.class).to(UserProjectFieldExportOptionsHibernateDAO.class);
        bind(ObjectAcidVersionDAO.class).to(ObjectAcidVersionHibernateDAO.class);
        bind(ObjectStringVersionDAO.class).to(ObjectStringVersionHibernateDAO.class);
        bind(ObjectVersionDAO.class).to(ObjectVersionHibernateDAO.class);
        bind(EventTypeDAO.class).to(EventTypeHibernateDAO.class);
        bind(ObjectEventDAO.class).to(ObjectEventHibernateDAO.class);
    }
}
