package org.example;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.digitaltwin.basyx.aasenvironment.client.ConnectedAasManager;
import org.eclipse.digitaltwin.basyx.aasrepository.client.ConnectedAasRepository;
import org.eclipse.digitaltwin.basyx.aasservice.client.ConnectedAasService;
import org.eclipse.digitaltwin.basyx.operation.InvokableOperation;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;
import org.eclipse.digitaltwin.basyx.submodelservice.client.ConnectedSubmodelService;

import java.util.List;

public class Main {
    static void main() {

        //SEE POM.XML FOR NECESSARY DEPENDENCY BASYX LIBRARIES AND AAS4J OBJECT MODEL
        //THIS CODE NEEDS BASYX [AAS REPOSITORY] , [SUBMODEL REPOSITORY] , [AAS REGISTRY] and [SUBMODEL REGISTRY] RUNNING
        //SEE basyx.org FOR BASYX OFF THE SHELF DOCKER CONTAINER DOWNLOADS

        //Connect to AAS Environment (BaSyx Servers)
        String aasRepositoryBaseUrl = "http://localhost:8081";
        String submodelRepositoryBaseUrl = "http://localhost:8081";

        ConnectedAasRepository helloAasRepo = new ConnectedAasRepository(aasRepositoryBaseUrl);
        ConnectedSubmodelRepository helloSubmodelRepo = new ConnectedSubmodelRepository(submodelRepositoryBaseUrl);

        AssetInformation assetInfo = new DefaultAssetInformation.Builder()
                .assetKind(AssetKind.TYPE)
                .build();

        //Create AAS in Java Object Model
        AssetAdministrationShell helloAAS= new DefaultAssetAdministrationShell.Builder()
                .id("http://example.com/aas/helloWorld")
                .idShort("HelloWorldAAS")
                .assetInformation(assetInfo)
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

        helloAasRepo.createAas(helloAAS);
        helloSubmodelRepo.createSubmodel(helloSubmodel);
        helloAasRepo.addSubmodelReference(helloAAS.getId(),helloSubmodel.getSemanticId());

        //Create new SubmodelElementCollection (SMC) in Java Object Model
        SubmodelElementCollection helloCollection =  new DefaultSubmodelElementCollection.Builder()
                .idShort("helloSMCollection")
                .value(List.of())
                .build();

        //Add SubmodelElementCollection (SMC) from Java Object to Submodel
        helloSubmodelRepo.createSubmodelElement(helloSubmodel.getId(),helloCollection);

        //Create Invokable Operation
        DefaultOperationVariable input_A = new DefaultOperationVariable.Builder().value(new DefaultProperty.Builder().idShort("A").valueType(DataTypeDefXsd.INT).build()).build();
        DefaultOperationVariable input_B = new DefaultOperationVariable.Builder().value(new DefaultProperty.Builder().idShort("B").valueType(DataTypeDefXsd.INT).build()).build();
        DefaultOperationVariable result_C = new DefaultOperationVariable.Builder().value(new DefaultProperty.Builder().idShort("C").valueType(DataTypeDefXsd.INT).build()).build();

        Operation helloOperation = new InvokableOperation.Builder()
                .idShort("pythagorasOperation")
                .inputVariables(input_A)
                .inputVariables(input_B)
                .outputVariables(result_C)
                .invokable(Main::pythagoras)
                .build();

        //Get Submodel Element from Submodel Service (Submodel Server) and add a Property to the SMC (Update), then re-push it to the Server
        SubmodelElementCollection helloSMC = (SubmodelElementCollection) helloSubmodelRepo.getSubmodelElement(helloSubmodel.getId(),"helloSMCollection");
        helloSMC.getValue().add(new DefaultProperty.Builder().idShort("helloProperty").valueType(DataTypeDefXsd.STRING).value("h3ll0World!").build());
        helloSMC.getValue().add(helloOperation);
        helloSubmodelRepo.updateSubmodelElement(helloSubmodel.getId(),"helloSMCollection", helloSMC);

        //STOP EXECUTION HERE TO SEE RESULTS
        Integer addBreakpointHere = 42;
        //REST OF EXAMPLE CODE WILL DELETE EVERYTHING CREATED

        //Remove Submodel from AAS
        helloAasRepo.removeSubmodelReference(helloAAS.getId(),helloSubmodel.getId());

        //Delete the Property
        helloSubmodelRepo.deleteSubmodelElement(helloSubmodel.getId(),"helloSMCollection.helloProperty");

        //Delete whole Submodel
        helloSubmodelRepo.deleteSubmodel(helloSubmodel.getId());

        //Delete whole AAS
        helloAasRepo.deleteAas(helloAAS.getId());
    }

    //Invokable Operation: Pythagoras
    private static OperationVariable[] pythagoras(OperationVariable[] inputs) {
        Property A = (Property) inputs[0].getValue();
        Property B = (Property) inputs[1].getValue();
        Property C = (Property) inputs[2].getValue();
        Integer iA = Integer.valueOf(A.getValue());
        Integer iB = Integer.valueOf(B.getValue());

        Integer A_squared = iA * iA;
        Integer B_squared = iB * iB;
        Integer C_squared = A_squared + B_squared;
        Integer C_out = (int) Math.sqrt(C_squared);

        C.setValue(C_out.toString());
        C.setIdShort("C");

        OperationVariable result = new DefaultOperationVariable.Builder().value(C).build();

        return new OperationVariable[] { result };
    }
}
