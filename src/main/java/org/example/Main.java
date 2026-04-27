package org.example;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.digitaltwin.basyx.aasenvironment.client.ConnectedAasManager;
import org.eclipse.digitaltwin.basyx.aasservice.client.ConnectedAasService;
import org.eclipse.digitaltwin.basyx.submodelservice.client.ConnectedSubmodelService;

import java.util.List;

public class Main {
    static void main() {

        //SEE POM.XML FOR NECESSARY DEPENDENCY BASYX LIBRARIES AND AAS4J OBJECT MODEL
        //THIS CODE NEEDS BASYX [AAS REPOSITORY] , [SUBMODEL REPOSITORY] , [AAS REGISTRY] and [SUBMODEL REGISTRY] RUNNING
        //SEE basyx.org FOR BASYX OFF THE SHELF DOCKER CONTAINER DOWNLOADS

        //Connect to AAS Environment (BaSyx Servers)
        String aasRegistryBaseUrl = "http://localhost:8082";
        String aasRepositoryBaseUrl = "http://localhost:8081";
        String submodelRegistryBaseUrl = "http://localhost:8083";
        String submodelRepositoryBaseUrl = "http://localhost:8081";
        ConnectedAasManager helloManager = new ConnectedAasManager(aasRegistryBaseUrl, aasRepositoryBaseUrl, submodelRegistryBaseUrl, submodelRepositoryBaseUrl);

        //Create AAS in Java Object Model
        AssetAdministrationShell helloAAS= new DefaultAssetAdministrationShell.Builder()
                .id("http://example.com/aas/helloWorld")
                .idShort("HelloWorldAAS")
                .build();

        //Create SemanticId for Submodel
        DefaultKey key = new DefaultKey.Builder().type(KeyTypes.SUBMODEL).value("http://example.com/aas/helloWorld/submodel").build();
        DefaultReference ref = new DefaultReference.Builder().type(ReferenceTypes.EXTERNAL_REFERENCE).keys(key).build();

        //Create empty Submodel in Java Object Model
        Submodel helloSubmodel = new DefaultSubmodel.Builder()
                .id("http://example.com/aas/helloWorld/submodel")
                .idShort("helloSubmodel")
                .semanticId(ref)
                .build();

        helloManager.createAas(helloAAS);
        helloManager.createSubmodelInAas(helloAAS.getId(), helloSubmodel);

        //Initialize Service Managers
        ConnectedAasService helloAASService = helloManager.getAasService(helloAAS.getId());
        ConnectedSubmodelService helloSMService = helloManager.getSubmodelService(helloSubmodel.getId());

        //Create new SubmodelElementCollection (SMC) in Java Object Model
        SubmodelElementCollection helloCollection =  new DefaultSubmodelElementCollection.Builder()
                .idShort("helloSMCollection")
                .value(List.of())
                .build();

        //Add SubmodelElementCollection (SMC) from Java Object to Submodel
        helloSMService.createSubmodelElement(helloCollection);

        //Get Submodel Element from Submodel Service (Submodel Server) and add a Property to the SMC (Update), then re-push it to the Server
        SubmodelElementCollection helloSMC = (SubmodelElementCollection) helloSMService.getSubmodelElement("helloSMCollection");
        helloSMC.getValue().add(
                new DefaultProperty.Builder().idShort("helloProperty").valueType(DataTypeDefXsd.STRING).value("h3ll0World!").build());
        helloSMService.updateSubmodelElement("helloSMCollection", helloSMC);

        //STOP EXECUTION HERE TO SEE RESULTS
        Integer addBreakpointHere = 42;
        //REST OF EXAMPLE CODE WILL DELETE EVERYTHING CREATED

        //Delete the Property
        helloSMService.deleteSubmodelElement("helloSMCollection.helloProperty");

        //Delete whole Submodel
        helloManager.deleteSubmodelOfAas(helloAAS.getId(),helloSubmodel.getId());

        //Delete whole AAS
        helloManager.deleteAas(helloAAS.getId());
    }
}
