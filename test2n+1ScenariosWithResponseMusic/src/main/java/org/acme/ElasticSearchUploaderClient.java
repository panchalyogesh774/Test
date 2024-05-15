// package org.acme;

// import java.util.List;

// import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

// import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.ws.rs.Consumes;
// import jakarta.ws.rs.POST;
// import jakarta.ws.rs.Path;

// @Path("/elts")
// @RegisterRestClient
// @ApplicationScoped
// public interface ElasticSearchUploaderClient {

//     @POST
//     @Path("/_bulk")
//     @Consumes("application/json")
//     void uploadCsvToElasticsearch(List<String> jsonLines);
// }