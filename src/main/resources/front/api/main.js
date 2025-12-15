/**
 * 获取所有的菜品分类
 * @returns {Promise} 菜品分类列表
 */
function categoryListApi() {
    return $axios({
      'url': '/category/list',
      'method': 'get',
    })
  }

/**
 * 获取菜品分类对应的菜品
 * @param {Object} data 查询参数（categoryId:分类ID, status:状态）
 * @returns {Promise} 菜品列表
 */
function dishListApi(data) {
    return $axios({
        'url': '/dish/list',
        'method': 'get',
        params:{...data}
    })
}

/**
 * 获取菜品分类对应的套餐
 * @param {Object} data 查询参数（categoryId:分类ID, status:状态）
 * @returns {Promise} 套餐列表
 */
function setmealListApi(data) {
    return $axios({
        'url': '/setmeal/list',
        'method': 'get',
        params:{...data}
    })
}

/**
 * 获取购物车内商品的集合
 * @param {Object} data 查询参数
 * @returns {Promise} 购物车商品列表
 */
function cartListApi(data) {
    return $axios({
        'url': '/shoppingCart/list',
        //'url': '/front/cartData.json',
        'method': 'get',
        params:{...data}
    })
}

/**
 * 添加商品到购物车
 * @param {Object} data 商品信息（dishId/setmealId, dishFlavor, name, image, amount）
 * @returns {Promise} 添加结果
 */
function  addCartApi(data){
    return $axios({
        'url': '/shoppingCart/add',
        'method': 'post',
        data
      })
}

/**
 * 减少购物车中商品数量
 * @param {Object} data 商品ID（dishId或setmealId）
 * @returns {Promise} 更新结果
 */
function  updateCartApi(data){
    return $axios({
        'url': '/shoppingCart/sub',
        'method': 'post',
        data
      })
}

/**
 * 清空购物车
 * @returns {Promise} 清空结果
 */
function clearCartApi() {
    return $axios({
        'url': '/shoppingCart/clean',
        'method': 'delete',
    })
}

/**
 * 获取套餐的全部菜品
 * @param {Number} id 套餐ID
 * @returns {Promise} 套餐包含的菜品列表
 */
function setMealDishDetailsApi(id) {
    return $axios({
        'url': `/setmeal/dish/${id}`,
        'method': 'get',
    })
}


