package org.example;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.digitaltwin.basyx.aasenvironment.client.ConnectedAasManager;

import java.util.List;
import java.util.Random;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {

    AssetAdministrationShell helloAAS= new DefaultAssetAdministrationShell.Builder()
            .id("http://example.com/aas/helloWorld")
            .idShort("HelloWorldAAS")
            .build();

    List<SubmodelElement> helloProperties = List.of(
            new DefaultProperty.Builder()
                    .idShort("SomeNewProperty")
                    .value("Hello")
                    .build());

    DefaultKey key = new DefaultKey.Builder().type(KeyTypes.CONCEPT_DESCRIPTION).value("https://admin-shell.io/zvei/nameplate/2/0/HelloWorld").build();
    DefaultReference ref = new DefaultReference.Builder().type(ReferenceTypes.EXTERNAL_REFERENCE).keys(key).build();

    Submodel helloSubmodel = new DefaultSubmodel.Builder()
            .id("http://example.com/aas/helloWorld/submodel")
            .idShort("Submodel1")
            .submodelElements(helloProperties)
            .semanticId(ref)
            .build();

    String aasRegistryBaseUrl = "http://localhost:8082";
    String aasRepositoryBaseUrl = "http://localhost:8081";
    String submodelRegistryBaseUrl = "http://localhost:8083";
    String submodelRepositoryBaseUrl = "http://localhost:8081";
    ConnectedAasManager helloManager = new ConnectedAasManager(aasRegistryBaseUrl, aasRepositoryBaseUrl, submodelRegistryBaseUrl, submodelRepositoryBaseUrl);

    try{
    helloManager.createAas(helloAAS);
    }catch(Exception e){ System.out.println(e);} //Problem: Exception in AAS-Registry: 409 obwohl neu

    try {
        helloManager.createSubmodelInAas(helloAAS.getId(), helloSubmodel);
    }catch(Exception e){ System.out.println(e);} //Problem: Exception in Submodel-Registry: 409 obwohl neu : Es wird danach KEINE Verknpüpfung zwischen AAS und Submodel gemacht!

    helloManager.deleteAas(helloAAS.getId());

    }
}
