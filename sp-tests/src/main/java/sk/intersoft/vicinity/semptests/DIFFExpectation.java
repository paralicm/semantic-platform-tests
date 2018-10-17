package sk.intersoft.vicinity.semptests;

import java.util.HashSet;
import java.util.Set;
import sk.intersoft.vicinity.agent.service.config.processor.ThingDescriptions;

public class DIFFExpectation {
        Set<String> delete = new HashSet<String>();
        Set<String> create = new HashSet<String>();
        Set<String> update = new HashSet<String>();
        Set<String> unchange = new HashSet<String>();

        ThingDescriptions things = new ThingDescriptions();
}