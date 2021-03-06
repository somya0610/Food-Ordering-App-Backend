package com.upgrad.FoodOrderingApp.service.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "restaurant_category")
@NamedQueries(
        {
                @NamedQuery(name = "categoriesByRestaurant",
                        query = "select rc from RestaurantCategoryEntity rc where rc.restaurant.uuid = :restaurantUuid order by rc.category.categoryName"),
                @NamedQuery(name = "restaurantsByCategory",
                        query = "select rc from RestaurantCategoryEntity rc where rc.category.uuid =:categoryUuid order by  rc.restaurant.restaurantName")
        }
)
public class RestaurantCategoryEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "RESTAURANT_ID")
    private RestaurantEntity restaurant;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "CATEGORY_ID")
    private CategoryEntity category;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RestaurantEntity getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(RestaurantEntity restaurant) {
        this.restaurant = restaurant;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RestaurantCategoryEntity that = (RestaurantCategoryEntity) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(restaurant, that.restaurant)
                .append(category, that.category)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(restaurant)
                .append(category)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("restaurant", restaurant)
                .append("category", category)
                .toString();
    }
}
