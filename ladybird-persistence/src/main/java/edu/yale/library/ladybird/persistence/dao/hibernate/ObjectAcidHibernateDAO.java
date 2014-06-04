package edu.yale.library.ladybird.persistence.dao.hibernate;

import edu.yale.library.ladybird.entity.ObjectAcid;
import edu.yale.library.ladybird.persistence.dao.ObjectAcidDAO;
import org.hibernate.Query;

public class ObjectAcidHibernateDAO extends GenericHibernateDAO<ObjectAcid, Integer> implements ObjectAcidDAO {

    @Override
    public ObjectAcid findByOidAndFdid(final int oid, final int fdid) {
        final Query q = getSession().createQuery("from ObjectAcid where objectId = :param1 and fdid =:param2");
        q.setParameter("param1", oid);
        q.setParameter("param2", fdid);
        return (ObjectAcid) q.list().get(0);
    }

}

