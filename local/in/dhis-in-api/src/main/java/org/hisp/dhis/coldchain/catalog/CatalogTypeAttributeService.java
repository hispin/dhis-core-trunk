package org.hisp.dhis.coldchain.catalog;

import java.util.Collection;

public interface CatalogTypeAttributeService
{
    String ID = CatalogTypeAttributeService.class.getName();
    
    int addCatalogTypeAttribute( CatalogTypeAttribute catalogTypeAttribute );

    void updateCatalogTypeAttribute( CatalogTypeAttribute catalogTypeAttribute );

    void deleteCatalogTypeAttribute( CatalogTypeAttribute catalogTypeAttribute );

    Collection<CatalogTypeAttribute> getAllCatalogTypeAttributes();

}
