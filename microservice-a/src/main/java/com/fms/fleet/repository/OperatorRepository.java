package com.fms.fleet.repository;
import com.fms.fleet.model.entity.Operator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jooq.DSLContext;
import org.jooq.Record;
import java.time.OffsetDateTime;
import java.util.*;

@ApplicationScoped
public class OperatorRepository {

    @Inject DSLContext dsl;
    private static final String SEL =
        "SELECT id,operator_type,first_name,last_name,employee_id,status,contact_info::text AS contact_info,created_at FROM operators";

    public List<Operator> findAll() { return dsl.fetch(SEL+" ORDER BY last_name,first_name").map(this::map); }
    public List<Operator> findAvailable() { return dsl.fetch(SEL+" WHERE status='AVAILABLE'").map(this::map); }
    public Optional<Operator> findById(UUID id) { return dsl.fetch(SEL+" WHERE id=?",id).stream().findFirst().map(this::map); }
    public Optional<Operator> findByEmployeeId(String eid) { return dsl.fetch(SEL+" WHERE employee_id=?",eid).stream().findFirst().map(this::map); }

    @Transactional
    public Operator create(Operator o) {
        UUID id=UUID.randomUUID();
        dsl.execute("INSERT INTO operators(id,operator_type,first_name,last_name,employee_id,status,contact_info,created_at) VALUES(?,?,?,?,?,?,?::jsonb,?)",
                id,o.operatorType(),o.firstName(),o.lastName(),o.employeeId(),
                o.status()!=null?o.status():"AVAILABLE",o.contactInfo(),OffsetDateTime.now());
        return findById(id).orElseThrow();
    }

    @Transactional
    public Optional<Operator> update(UUID id, Operator o) {
        int r=dsl.execute("UPDATE operators SET first_name=?,last_name=?,status=?,contact_info=?::jsonb WHERE id=?",
                o.firstName(),o.lastName(),o.status(),o.contactInfo(),id);
        return r==0?Optional.empty():findById(id);
    }

    @Transactional
    public void updateStatus(UUID id, String status) { dsl.execute("UPDATE operators SET status=? WHERE id=?",status,id); }

    @Transactional
    public boolean delete(UUID id) { return dsl.execute("DELETE FROM operators WHERE id=?",id)>0; }

    private Operator map(Record r) {
        return new Operator(r.get("id",UUID.class),r.get("operator_type",String.class),
                r.get("first_name",String.class),r.get("last_name",String.class),
                r.get("employee_id",String.class),r.get("status",String.class),
                r.get("contact_info",String.class),r.get("created_at",OffsetDateTime.class));
    }
}
