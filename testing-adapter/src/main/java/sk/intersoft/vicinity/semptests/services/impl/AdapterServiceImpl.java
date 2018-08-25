/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.intersoft.vicinity.semptests.services.impl;

import sk.intersoft.vicinity.semptests.pojos.Response;

import sk.intersoft.vicinity.semptests.services.AdapterService;

import sk.intersoft.vicinity.semptests.pojos.Requests;
import org.apache.commons.io.IOUtils;

import org.apache.log4j.Logger;

/**
 *
 * @author dzandes
 */
public class AdapterServiceImpl implements AdapterService {

    private static final Logger LOG = Logger.getLogger(AdapterServiceImpl.class);
    public static String filename = "";

    @Override
    public Response postAction(String oid, String aid, Requests.ActionRequest request) {

        LOG.info("Inside postAction - TBD by each pilot depending on their VAS...");

        Response resp_ = new Response("Not supported yet."); // TODO
        return resp_;
    }

    @Override
    public Response getActionTask(String oid, String aid, String tid) {

        LOG.info("Inside getActionTask - TBD by each pilot depending on their VAS...");

        Response resp_ = new Response("Not supported yet."); // TODO
        return resp_;
    }

    @Override
    public Response deleteActionTask(String oid, String aid, String tid) {

        LOG.info("Inside deleteActionTask - TBD by each pilot depending on their VAS...");

        Response resp_ = new Response("Not supported yet."); // TODO
        return resp_;
    }

    @Override
    public Response getProperty(String oid, String pid) {

        LOG.info("Inside getProperty - TBD by each pilot depending on their VAS...");

        Response resp_ = new Response("Not supported yet."); // TODO
        return resp_;
    }

    @Override
    public Response putProperty(String oid, String pid, Requests.PropertyRequest request) {

        LOG.info("Inside putProperty - TBD by each pilot depending on their VAS...");

        Response resp_ = new Response("Not supported yet."); // TODO
        return resp_;
    }

    @Override
    public Response putEvent(String oid, String eid, Requests.PropertyRequest request) {

        LOG.info("Inside putEvent - TBD by each pilot depending on their VAS...");

        Response resp_ = new Response("Not supported yet."); // TODO
        return resp_;
    }

    @Override
    public Response getThingsDescription() {

        LOG.info("Inside getThingsDescription - Semantic tests: " + filename);

        ClassLoader cl = getClass().getClassLoader();

        String thingsDescription = "";
        try {
            thingsDescription = IOUtils
                    //.toString(cl.getResourceAsStream("td-sample.json"));
                    .toString(cl.getResourceAsStream(filename));
        } catch (Exception ex) {
            LOG.error("Unable to load Things Description...");
            ex.printStackTrace();
        }

        Response resp_ = new Response(thingsDescription);
        return resp_;
    }

}