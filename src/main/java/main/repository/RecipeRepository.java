package main.repository;

import main.entities.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // Поиск по названию (частичное совпадение, нечувствительно к регистру)
    List<Recipe> findByTitleContainingIgnoreCase(String keyword);

    // Поиск по ингредиентам
    @Query("SELECT r FROM Recipe r WHERE LOWER(r.ingredients) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Recipe> findByIngredientContainingIgnoreCase(@Param("keyword") String keyword);

    // Поиск по категории
    List<Recipe> findByCategoryIgnoreCase(String category);

    List<Recipe> findByFavoriteTrue();

    @Query("select r from Recipe r where upper(r.ingredients) like upper(concat('%', ?1, '%'))")
    List<Recipe> findByIngredientsContainsIgnoreCase(String ingredients);
}


