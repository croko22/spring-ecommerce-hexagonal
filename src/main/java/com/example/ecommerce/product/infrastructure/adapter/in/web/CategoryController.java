package com.example.ecommerce.product.infrastructure.adapter.in.web;

import com.example.ecommerce.product.application.port.in.CreateCategoryUseCase;
import com.example.ecommerce.product.application.port.in.DeleteCategoryUseCase;
import com.example.ecommerce.product.application.port.in.GetCategoryUseCase;
import com.example.ecommerce.product.application.port.in.UpdateCategoryUseCase;
import com.example.ecommerce.product.domain.model.Category;
import com.example.ecommerce.product.infrastructure.adapter.in.web.dto.CategoryRequest;
import com.example.ecommerce.product.infrastructure.adapter.in.web.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final GetCategoryUseCase getCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;

    public CategoryController(CreateCategoryUseCase createCategoryUseCase,
                              GetCategoryUseCase getCategoryUseCase,
                              UpdateCategoryUseCase updateCategoryUseCase,
                              DeleteCategoryUseCase deleteCategoryUseCase) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.getCategoryUseCase = getCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.deleteCategoryUseCase = deleteCategoryUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a category", description = "Creates a new category with name and optional description")
    public CategoryResponse createCategory(@RequestBody CategoryRequest categoryRequest) {
        Category category = new Category(null, categoryRequest.getName(), categoryRequest.getDescription());
        Category saved = createCategoryUseCase.createCategory(category);
        return toResponse(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a category by ID", description = "Returns a single category")
    public CategoryResponse getCategory(@PathVariable Long id) {
        Category category = getCategoryUseCase.getCategoryById(id);
        return toResponse(category);
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Returns a list of all categories")
    public List<CategoryResponse> getAllCategories() {
        return getCategoryUseCase.getAllCategories().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category", description = "Updates an existing category")
    public CategoryResponse updateCategory(@PathVariable Long id, @RequestBody CategoryRequest categoryRequest) {
        Category category = new Category(id, categoryRequest.getName(), categoryRequest.getDescription());
        Category updated = updateCategoryUseCase.updateCategory(id, category);
        return toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a category", description = "Deletes a category by ID")
    public void deleteCategory(@PathVariable Long id) {
        deleteCategoryUseCase.deleteCategory(id);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
