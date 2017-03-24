/*
 * Copyright 2017 Crown Copyright
 *
 * This file is part of Stroom-Stats.
 *
 * Stroom-Stats is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Stroom-Stats is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Stroom-Stats.  If not, see <http://www.gnu.org/licenses/>.
 */

package stroom.stats;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.stats.config.Config;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.function.Supplier;

public abstract class AbstractAppIT {

    Logger LOGGER = LoggerFactory.getLogger(AbstractAppIT.class);

    private static Client client;
    private static App app;

    protected static String STATISTICS_URL;
    protected static String QUERY_URL;

    //TODO this may prevent parallel execution of test classes
    @BeforeClass
    public static void setupClass() {
        // We need to enable typing otherwise abstract types, e.g. ExpressionItem, won't be deserialisable.
        RULE.getEnvironment().getObjectMapper().enableDefaultTyping();
        app = RULE.getApplication();
        client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");
        STATISTICS_URL = String.format("http://localhost:%d/statistics", RULE.getLocalPort());
        QUERY_URL = String.format("http://localhost:%d/search", RULE.getLocalPort());
    }

    @ClassRule
    public static final DropwizardAppRule<Config> RULE = new DropwizardAppRule<>(App.class, "config_dev.yml");


    protected static Client getClient() {
        return client;
    }

    public static DropwizardAppRule<Config> getAppRule() {
        return RULE;
    }

    public static App getApp() {
        return app;
    }

    protected static Response post(Supplier<Serializable> requestObjectFunc, String url, String mediaType){
        return post(requestObjectFunc, url, mediaType, AuthorizationHeader.VALID);
    }

    protected static Response post(Supplier<Serializable> requestObjectFunc, String url, String mediaType, AuthorizationHeader authorizationHeader){
        switch(mediaType){
            case MediaType.APPLICATION_JSON: return postJson(requestObjectFunc.get(), url, authorizationHeader.get());
            case MediaType.APPLICATION_XML: return postXml(requestObjectFunc.get(), url, authorizationHeader.get());
            default: throw new RuntimeException("Unsupported media type: " + mediaType);
        }
    }

    protected static Response postJson(Serializable requestObject, String url, String authorizationHeader){
        Response response = getClient().target(url)
                .request()
                .header("Authorization", authorizationHeader)
                .post(Entity.json(requestObject));
        return response;
    }

    protected static Response postXml(Serializable requestObject, String url, String authorizationHeader){
        Response response = getClient().target(url)
                .request()
                .header("Authorization", authorizationHeader)
                .post(Entity.xml(requestObject));
        return response;
    }

}
