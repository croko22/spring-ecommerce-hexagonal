package com.example.ecommerce.product.infrastructure.adapter.in.web;

import com.example.ecommerce.product.application.port.in.CreateCategoryUseCase;
import com.example.ecommerce.product.application.port.in.DeleteCategoryUseCase;
import com.example.ecommerce.product.application.port.in.GetCategoryUseCase;
import com.example.ecommerce.product.application.port.in.UpdateCategoryUseCase;
import com.example.ecommerce.product.domain.exception.CategoryNotFoundException;
import com.example.ecommerce.product.domain.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateCategoryUseCase createCategoryUseCase;

    @Mock
    private GetCategoryUseCase getCategoryUseCase;

    @Mock
    private UpdateCategoryUseCase updateCategoryUseCase;

    @Mock
    private DeleteCategoryUseCase deleteCategoryUseCase;

    @BeforeEach
    void setUp() {
        CategoryController categoryController = new CategoryController(
                createCategoryUseCase,
                getCategoryUseCase,
                updateCategoryUseCase,
                deleteCategoryUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateCategoryAndReturn201() throws Exception {
        String requestBody = "{\"name\":\"Electronics\",\"description\":\"Devices and gadgets\"}";
        Category category = new Category(1L, "Electronics", "Devices and gadgets");

        when(createCategoryUseCase.createCategory(any(Category.class))).thenReturn(category);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.description").value("Devices and gadgets"));
    }

    @Test
    void shouldGetCategoryByIdAndReturn200() throws Exception {
        Category category = new Category(1L, "Electronics", "Devices and gadgets");
        when(getCategoryUseCase.getCategoryById(1L)).thenReturn(category);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        when(getCategoryUseCase.getCategoryById(99L)).thenThrow(new CategoryNotFoundException(99L));

        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Category with ID 99 not found"));
    }

    @Test
    void shouldReturnAllCategoriesAndReturn200() throws Exception {
        List<Category> categories = List.of(
                new Category(1L, "Electronics", "Devices"),
                new Category(2L, "Books", "Reading")
        );
        when(getCategoryUseCase.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[1].name").value("Books"));
    }

    @Test
    void shouldUpdateCategoryAndReturn200() throws Exception {
        String requestBody = "{\"name\":\"Updated Electronics\",\"description\":\"Updated description\"}";
        Category updated = new Category(1L, "Updated Electronics", "Updated description");

        when(updateCategoryUseCase.updateCategory(eq(1L), any(Category.class))).thenReturn(updated);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Electronics"));
    }

    @Test
    void shouldDeleteCategoryAndReturn204() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());

        verify(deleteCategoryUseCase).deleteCategory(1L);
    }

    @Test
    void shouldReturn400WhenCreatingCategoryWithBlankName() throws Exception {
        String requestBody = "{\"name\":\"\",\"description\":\"Invalid\"}";

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Category name cannot be blank"));
    }
}
