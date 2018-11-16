package sk.intersoft.vicinity.semptests;

import sk.intersoft.vicinity.agent.service.config.processor.ThingDescriptions;

import java.util.HashSet;
import java.util.Set;

public class DIFFExpectation {
        Set<String> delete = new HashSet<String>();
        Set<String> create = new HashSet<String>();
        Set<String> update = new HashSet<String>();
        Set<String> unchange = new HashSet<String>();

        ThingDescriptions things = new ThingDescriptions();
}