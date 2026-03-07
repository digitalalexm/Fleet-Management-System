package com.fms.simulator.service;
import com.fms.simulator.model.SimulationSession;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe registry of active simulation sessions keyed by tripId. */
@ApplicationScoped
public class SessionRegistry {
    private static final Logger LOG = Logger.getLogger(SessionRegistry.class);
    private final Map<UUID,SimulationSession> sessions = new ConcurrentHashMap<>();

    public void register(SimulationSession s) { sessions.put(s.tripId(),s); LOG.infof("Session started: trip=%s assetType=%s",s.tripId(),s.assetType()); }
    public void update(SimulationSession s) { sessions.put(s.tripId(),s); }
    public Optional<SimulationSession> deactivate(UUID tripId) { SimulationSession r=sessions.remove(tripId); if(r!=null) LOG.infof("Session ended: trip=%s",tripId); return Optional.ofNullable(r); }
    public Collection<SimulationSession> getAll() { return Collections.unmodifiableCollection(sessions.values()); }
    public Optional<SimulationSession> get(UUID tripId) { return Optional.ofNullable(sessions.get(tripId)); }
    public int count() { return sessions.size(); }
}
