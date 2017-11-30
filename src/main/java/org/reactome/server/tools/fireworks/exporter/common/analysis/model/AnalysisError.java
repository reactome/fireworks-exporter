package org.reactome.server.tools.fireworks.exporter.common.analysis.model;

import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public interface AnalysisError {

    Integer getCode();

    String getReason();

    List<String> getMessages();
}
