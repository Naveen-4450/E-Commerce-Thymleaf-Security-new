package com.example.controllers;

import com.example.models.dbModels.Categories;
import com.example.models.dtoModels.CategoriesDto;
import com.example.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@Tag(name = "Category Controller Api's", description = "This Api's are managed to the Categories of the Application")
public class CategoryController {
    @Autowired
    private CategoryService categorySer;

    @GetMapping("/addCategory")
    public String addingCategory()
    {
        return "addCategory";
    }
    @Operation(summary = "Add a new Category", description = "Adds a new Category in the Application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201" , description = "Category Added Successfully"),
           // @ApiResponse(responseCode = "400", description = "Invalid Input")
    })
    @PostMapping("/addCategory")
    public String addingCategory(@ModelAttribute CategoriesDto cateDto, Model model)
    {
        String msg = "";
      try {
          Categories category = categorySer.addingCategory(cateDto);
          msg = "Category Added Successfully";
          msg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
          return "redirect:/addCategory?status=success&msg=" + msg;

      } catch (Exception e) {
          e.printStackTrace();
          msg = "Category Adding Failed";
          try {
              msg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
          } catch (Exception ex) {
              ex.printStackTrace();
          }
          return "redirect:/addCategory?status=fail&msg=" + msg;
      }
    }


    @Operation(summary = "Fetch All Categories",description = "Fetch the all Categories available in Application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories Retrieved Successfully"),
            @ApiResponse(responseCode = "404", description = "Categories Not Available")
    })
    @GetMapping("/viewCategories")
    public String getAllCategories(Model model)
    {
        List<Categories> categories = categorySer.getAllCategories();
        model.addAttribute("categories",categories);
        return "viewCategories";
    }



    @GetMapping("/updateCategory/{id}")
    public String updateCategory(@PathVariable int id,Model model)
    {
        Categories category = categorySer.getCategoryById(id);
        if(category != null){
            model.addAttribute("category",category);
            return "updateCategory";
        }else{
            return "redirect:/viewCategories";
        }
    }
    @Operation(summary = "Update a Category",description = "Updating an existing Category in the Application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Category Updated Successfully"),
            @ApiResponse(responseCode = "404",description = "Category Not Found"),
           // @ApiResponse(responseCode = "400", description = "Invalid Input")
    })
    @PostMapping("/updateCategory/{id}")
    public String updatingCategory(@Parameter(description = "Enter the Id of Category to update")
                                                       @PathVariable int id,
                                                       @ModelAttribute CategoriesDto cDto)
    {
        categorySer.updatingCategory(id,cDto);
        String msg = "Category Updated Successfully";
        msg = URLEncoder.encode(msg,StandardCharsets.UTF_8);
        return "redirect:/viewCategories?status=success&msg="+msg;
    }



    @Operation(summary = "Get One Category by name", description = "Retrieves the Category by it's name from the Application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Category Found"),
            @ApiResponse(responseCode = "404", description = "Category not found with your name")
    })
    @GetMapping("/getOne")
    public ResponseEntity<Categories> getCategory(
            @Parameter(description = "Name of the Category to Retrieve") @RequestParam String categoryName)
    {
        return categorySer.getCategory(categoryName);
    }

}

