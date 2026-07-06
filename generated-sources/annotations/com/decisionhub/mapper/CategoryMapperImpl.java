package com.decisionhub.mapper;

import com.decisionhub.dto.CategoryRequest;
import com.decisionhub.dto.CategoryResponse;
import com.decisionhub.entity.Category;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-05T11:41:10+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public CategoryResponse toResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        UUID parentCategoryId = null;
        String parentCategoryName = null;
        UUID id = null;
        String name = null;
        String icon = null;
        String description = null;
        Instant createdAt = null;

        parentCategoryId = categoryParentCategoryId( category );
        parentCategoryName = categoryParentCategoryName( category );
        id = category.getId();
        name = category.getName();
        icon = category.getIcon();
        description = category.getDescription();
        createdAt = category.getCreatedAt();

        CategoryResponse categoryResponse = new CategoryResponse( id, name, icon, description, parentCategoryId, parentCategoryName, createdAt );

        return categoryResponse;
    }

    @Override
    public Category toEntity(CategoryRequest request) {
        if ( request == null ) {
            return null;
        }

        Category category = new Category();

        category.setName( request.name() );
        category.setIcon( request.icon() );
        category.setDescription( request.description() );

        return category;
    }

    @Override
    public void updateEntity(CategoryRequest request, Category category) {
        if ( request == null ) {
            return;
        }

        if ( request.name() != null ) {
            category.setName( request.name() );
        }
        if ( request.icon() != null ) {
            category.setIcon( request.icon() );
        }
        if ( request.description() != null ) {
            category.setDescription( request.description() );
        }
    }

    private UUID categoryParentCategoryId(Category category) {
        Category parentCategory = category.getParentCategory();
        if ( parentCategory == null ) {
            return null;
        }
        return parentCategory.getId();
    }

    private String categoryParentCategoryName(Category category) {
        Category parentCategory = category.getParentCategory();
        if ( parentCategory == null ) {
            return null;
        }
        return parentCategory.getName();
    }
}
