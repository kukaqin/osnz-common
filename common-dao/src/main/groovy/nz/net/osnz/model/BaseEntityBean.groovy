package nz.net.osnz.model

import groovy.transform.CompileStatic

import javax.persistence.*

/**
 * @author Kefeng Deng
 *
 * This is a base class for common Domain entity properties and behaviour.
 *
 */
@CompileStatic
@MappedSuperclass
public abstract class BaseEntityBean implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = 'ID')
    Long id

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = 'CREATED_ON', nullable = false)
    Date dateCreated = new Date()

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = 'UPDATED_ON', nullable = false)
    Date dateUpdated = new Date()

    @Column(name = 'ACTIVE', nullable = false)
    Boolean enabled = true

    @PrePersist
    public void onCreate() {
        this.dateCreated = this.dateUpdated = new Date()
    }

    @PreUpdate
    public void onUpdate() {
        this.dateUpdated = new Date()
    }

}
