package org.reactome.server.tools.fireworks.exporter.common.analysis.model;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public interface PathwayBase {

    String getStId();

    Long getDbId();

    EntityStatistics getEntities();

}
