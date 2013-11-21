package nz.net.osnz.sql

import groovy.sql.Sql
import org.hibernate.FlushMode
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.orm.hibernate4.SessionFactoryUtils
import org.springframework.orm.hibernate4.SessionHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronizationManager

import javax.inject.Inject
import java.sql.Connection
import java.sql.Statement

/**
 * @Author Kefeng Deng
 */
@Component("sqlService")
class SqlService {
    private static final Logger LOG = LoggerFactory.getLogger(SqlService)

    @Inject
    SessionFactory sessionFactory

    public String oracleClobToString(clob) {
        return clob.stringValue()
    }

    public void executeSql(String sql, String error) {
        process() { Connection connection ->
            // this is *so* much faster we'd be crazy not to use it
            Statement s = null
            try {
                s = connection.createStatement()
                s.executeUpdate(sql)
            } catch (Exception ex) {
                LOG.warn(error, ex)
            } finally {
                if (s) s.close()
            }
        }
    }

    public void paramSql(String sql, List<Object> params, Closure c) {
        process() { Connection connection ->
            // this is *so* much faster we'd be crazy not to use it
            Sql q = null
            try {
                q = new Sql(connection)
                q.eachRow(sql, params, c)
            } catch (Exception ex) {
                LOG.warn("Unable to process ${sql}", ex)
            } finally {
                if (q) q.close()
            }
        }
    }

    public void groovySql(String sql, String error, Closure c) {
        process() { Connection connection ->
            // this is *so* much faster we'd be crazy not to use it
            Sql q = null
            try {
                q = new Sql(connection)
                q.eachRow(sql, c)
            } catch (Exception ex) {
                LOG.warn(error, ex)
            } finally {
                if (q) q.close()
            }
        }
    }

    public void doInTransaction(Closure c) {

        Session session = sessionFactory.openSession()
        session.setFlushMode(FlushMode.AUTO)
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session))

        try {
            c()
        } finally {
            SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory)
            if (!FlushMode.MANUAL.equals(sessionHolder.getSession().getFlushMode())) {
                sessionHolder.getSession().flush()
            }
            TransactionSynchronizationManager.unbindResource(sessionFactory)
            SessionFactoryUtils.closeSession(sessionHolder.getSession())
        }
    }

    /**
     * Return a connection within current session
     */
    protected void process(Closure c) {
        Session session = sessionFactory.openSession()
        Connection connection = session.connection()
        try {
            c(connection)
        } finally {
            connection.close()
            session.close()
        }
    }

}