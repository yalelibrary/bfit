package edu.yale.library.ladybird.web.view;

import edu.yale.library.ladybird.entity.FieldMarcMapping;
import edu.yale.library.ladybird.entity.FieldMarcMappingBuilder;
import edu.yale.library.ladybird.persistence.dao.FieldMarcMappingDAO;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Osman Din {@literal <osman.din.yale@gmail.com>}
 */
@ManagedBean (name = "FieldMarcMappingView")
@RequestScoped
@SuppressWarnings("unchecked")
public class FieldMarcMappingView extends AbstractView {
    private final Logger logger = getLogger(this.getClass());

    private FieldMarcMapping item = new FieldMarcMappingBuilder().createFieldMarcMapping();
    private List<FieldMarcMapping> itemList;

    @Inject
    private FieldMarcMappingDAO entityDAO;

    @PostConstruct
    public void init() {
        initFields();
        dao = entityDAO;
    }

    public void save() {
        try {
            logger.debug("Saving item", item);
            setDefaults(item);
            dao.save(item);
        } catch (Throwable e) {
            logger.error("Error saving item", e);
        }
    }

    public List<FieldMarcMapping> getItemList() {
        List<FieldMarcMapping> list = dao.findAll();
        return list;
    }

    @Deprecated
    public void setDefaults(final FieldMarcMapping item) {
        final Date date = new Date(System.currentTimeMillis());
        item.setDate(date);
    }

    public FieldMarcMapping getItem() {
        return item;
    }
}


