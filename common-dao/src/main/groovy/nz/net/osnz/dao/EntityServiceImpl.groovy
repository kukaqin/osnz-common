package nz.net.osnz.dao

import groovy.transform.CompileStatic
import nz.ac.auckland.util.JacksonHelperApi
import nz.net.osnz.ajax.AjaxStatus
import nz.net.osnz.logger.LogService
import nz.net.osnz.model.BaseEntityBean
import org.hibernate.Criteria
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.criterion.Criterion
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import javax.inject.Inject
import javax.validation.ValidationException
import java.sql.SQLException

/**
 * @author Kefeng Deng
 *
 */
@Component('entityService')
@CompileStatic
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
class EntityServiceImpl implements EntityService {


    private static final Logger LOG = LoggerFactory.getLogger(EntityServiceImpl)

    @Inject
    LogService logService

    @Inject
    MessageSource messageSource

    @Inject
    SessionFactory sessionFactory

    @Inject
    JacksonHelperApi jacksonHelperApi

    protected Session getCurrentSession() {
        return sessionFactory.currentSession
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    BaseEntityBean findById(Class clazz, String id) {
        return (BaseEntityBean) currentSession.get(clazz, Long.parseLong(id))
    }

    @Override
    List<?> findBy(Class<?> clazz, Closure c = null) {
        Criteria criteria = currentSession.createCriteria(clazz)
        if ( c ) {
            try {
                c(criteria)
            } catch (SQLException e) {
                LOG.error "SQL Error of ${clazz.simpleName} because of ${e.message}"
            } catch (Exception e) {
                LOG.error "Error in ${clazz.simpleName} because of ${e.message}"
            }            
        }
        return criteria.list()
    }


    /**
     * Saves a specified entity, and if something goes wrong, returns an appropriate {@link #jsonResponse} map.
     * @param entity The entity to save.
     * @return Null if everything goes fine, or a response map if not.
     * @see #safePersist
     */
    @Override
    Map safePersistSaveOrUpdate(BaseEntityBean entity) {
        return safePersist(entity) {
            currentSession.saveOrUpdate( entity )
            currentSession.flush()
        }
    }

    /**
     * Deletes a specified entity, and if something goes wrong, returns an appropriate {@link #jsonResponse} map.
     * @param entity The entity to delete.
     * @return Null if everything goes fine, or a response map if not.
     * @see #safePersist
     */
    @Override
    Map safePersistDelete(BaseEntityBean entity) {
        return safePersist(entity) {
            currentSession.delete( entity )
            currentSession.flush()
        }
    }

    /**
     * This helper method, much like the forbidXxx ones, attempts to persist an entity, and if unsuccessful for some
     * reason, return a {@link #jsonResponse} map with error information after logging it. Will discard any changes to
     * the entity if an error occurs.
     * @param entity The entity to persist
     * @param operation The operation to perform on the entity.
     * @return Null if everything goes fine, or a response map if not.
     */
    protected Map safePersist(BaseEntityBean entity, Closure operation) {
        Map responseData = null

        try {
            operation(entity)
            currentSession.flush()
        } catch (ValidationException e) {
            responseData = [ message: e.message ]
            LOG.info logService.logMessage("Error validating ${entity.class.simpleName} changes because of ${e.message}", responseData)
        } catch (SQLException e) {
            responseData = [ message: e.message ]
            LOG.error logService.logMessage("SQL Error of ${entity.class.simpleName} changes because of ${e.message}", responseData)
        } catch (Exception e) {
            responseData = [ message: e.message ]
            LOG.error logService.logMessage("Error persisting ${entity.class.simpleName} changes because of ${e.message}", responseData)
        }

        if ( responseData ) {
            if ( entity.id != null ) {
                currentSession.evict( entity ) // To make sure hibernate doesn't trigger the error again
            }
            return [status: AjaxStatus.ERROR_DATABASE_PERSIST, data: responseData]
        }

        return [status: AjaxStatus.OK]
    }

    /**
     * Packages a map of parameters that can be fed into the Grails 'render' function in a Controller. Requires that
     * the AjaxStatus be set, but the response map is optional. Will always return an HTTP 200, and a content type of
     * 'application/json'. Uses Jackson to serialise the text output.
     * @param status The AjaxStatus of the reponse.
     * @param response A map containing arbitrary response data that will be serialised into JSON
     * @return A parameter Map for a Grails Controller render function call.
     * @see nz.ac.auckland.util.JacksonHelperApi
     */
    protected Map jsonResponse(AjaxStatus status, Map response = [ : ]) {

        return [
                status: status,
                text: jacksonHelperApi.jsonSerialize(response),
                contentType: 'application/json'
        ]
    }

    /**
     * Extract form errors and turn them into a map
     *
     * @param errorObjs are the error objects to investigate
     * @return a map with resolved error messages
     */
    protected Map formErrors(Locale locale = Locale.default, Errors... errorObjs) {
        List errors = errorObjs.collect { Errors errorObj ->
            return errorObj.fieldErrors.collect { FieldError error ->
                return [
                        field: error.field,
                        message: messageSource.getMessage(error, locale)
                ]
            }
        }.flatten()

        return [ errors: errors ]
    }


}
