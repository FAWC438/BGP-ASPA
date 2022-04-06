package core.routing

/**
 * RouteSelector 负责根据 [compare] 函数在路由 [table] 中选择最优先的路由。
 *
 * [compare] 函数应该采用两条路由并返回一个整数值来指示它们的顺序：
 *   - 正整数值表示第一个路由的优先级高于第二个
 *   - 零表示两条路线具有完全相同的偏好
 *   - 负整数值表示第一个路由的优先级低于第二个
 *
 * 它的工作方式类似于缓存。它跟踪选定的路由和邻居，直到它们变得无效。
 * 不是直接更新路由表，而是使用 [update] 方法，它会触发路由选择操作并更新路由表。
 * 这允许选择器以最有效的方式调整所选路线和邻居。
 * 警告：不要在选择器之外更新路由表，因为这样做会隐藏选择器的路由，这可能会导致意外行为。
 *
 * @property table 实际存储路由的底层路由表
 *
 * @constructor 在给定的 [table] 周围创建一个新的选择器。它不应该直接使用。请改用工厂方法。
 * @param forceReselect 如果为真，则选择器检查表中的所有路由并选择最喜欢的路由。否则，选择器从没有选择的路线开始。
 *
 */
class RouteSelector<R : Route> private constructor(
    val table: RoutingTable<R>,
    private val compare: (R, R) -> Int,
    forceReselect: Boolean = true
) {

    // 存储当前选择的路线
    private var selectedRoute: R = table.invalidRoute

    // 存储当前选择的邻居
    private var selectedNeighbor: Node<R>? = null

    /**
     * 记录被禁用的邻居。
     */
    private val mutableDisabledNeighbors = HashSet<Node<R>>()
    val disabledNeighbors: Collection<Node<R>> get() = mutableDisabledNeighbors

    init {
        if (forceReselect) {
            reselect()
        }
    }

    companion object Factory {

        /**
         * 根据新创建的路由表返回一个 [RouteSelector]。这是从新表创建选择器的推荐方法。
         *
         * @param invalid 新路由表设置为无效路由的路由
         * @param compare 比较路由的方法
         */
        fun <R : Route> wrapNewTable(invalid: R, compare: (R, R) -> Int): RouteSelector<R> {
            return RouteSelector(RoutingTable.empty(invalid), compare, forceReselect = false)
        }

        /**
         * 返回包装现有路由 [table] 的 [RouteSelector]。初始化时，选择器会遍历存储在 [table] 中的所有路由，并根据 [compare] 选择最佳路由。
         *
         * @param table   要被选择器包裹的表
         * @param compare 用于比较路线偏好的比较方法
         */
        fun <R : Route> wrap(table: RoutingTable<R>, compare: (R, R) -> Int): RouteSelector<R> {
            return RouteSelector(table, compare)
        }

    }

    /**
     * 返回当前选择的路线
     */
    // TODO @refactor - use a value property instead of the get method
    fun getSelectedRoute(): R = selectedRoute

    /**
     * 返回当前选择的邻居。
     */
    // TODO @refactor - use a value property instead of the get method
    fun getSelectedNeighbor(): Node<R>? = selectedNeighbor

    /**
     * 将 [neighbor] 的候选路由更新为 [route]。
     *
     * 此操作可能会触发对所选路由和邻居的更新。
     *
     * @return 如果所选路由邻居已更新，则为 true，否则为 false
     */
    fun update(neighbor: Node<R>, route: R): Boolean {

        table[neighbor] = route

        return if (table.isEnabled(neighbor) && compare(route, selectedRoute) > 0) {
            updateSelectedTo(route, neighbor)
            true

        } else if (neighbor == selectedNeighbor && compare(route, selectedRoute) != 0) {
            reselect()
            true

        } else {
            // do nothing
            false
        }
    }

    /**
     * 禁用 [neighbor]。禁用邻居会使该邻居及其各自的候选路由不符合选择条件，即使该路由是可用的最佳路由。
     * 也就是说，选择器永远不会从禁用的邻居中选择路由。
     *
     * 此操作可能会触发对所选路由和邻居的更新。
     *
     * @return 如果所选路由邻居已更新，则为 true，否则为 false
     */
    fun disable(neighbor: Node<R>): Boolean {

        table.setEnabled(neighbor, false)
        mutableDisabledNeighbors.add(neighbor)

        // 不需要检查节点是否已添加到禁用邻居集中：
        // 如果不是，则该邻居已被禁用，并且肯定不是选定的邻居

        if (neighbor == selectedNeighbor) {
            reselect()
            return true
        }

        return false
    }

    /**
     * 如果 [neighbor] 被禁用，则启用 [neighbor]。启用禁用的邻居可能会触发对所选路由和邻居的更新。启用已启用的邻居无效。
     *
     * @return 如果所选路由邻居已更新，则为 true，否则为 false
     */
    fun enable(neighbor: Node<R>): Boolean {

        table.setEnabled(neighbor, true)

        // 检查邻居是否真的从禁用集中删除可以避免在节点未禁用时进行表查找

        if (mutableDisabledNeighbors.remove(neighbor)) {

            val route = table[neighbor]

            if (compare(route, selectedRoute) > 0) {
                updateSelectedTo(route, neighbor)
                return true
            }
        }

        return false
    }

    /**
     * 启用当前禁用的所有邻居。
     *
     * @return 如果所选路由邻居已更新，则为 true，否则为 false
     */
    // TODO @refactor - remove this method because it is not used in production
    fun enableAll(): Boolean {

        var selectedRouteAmongDisabled = table.invalidRoute
        var selectedNeighborAmongDisabled: Node<R>? = null

        for (neighbor in mutableDisabledNeighbors) {
            val route = table.setEnabled(neighbor, true)

            if (compare(route, selectedRouteAmongDisabled) > 0) {
                selectedRouteAmongDisabled = route
                selectedNeighborAmongDisabled = neighbor
            }
        }

        // 如果我们启用所有可以清除该集合的邻居
        mutableDisabledNeighbors.clear()

        return if (compare(selectedRouteAmongDisabled, selectedRoute) > 0) {
            selectedRoute = selectedRouteAmongDisabled
            selectedNeighbor = selectedNeighborAmongDisabled
            true
        } else {
            false
        }
    }

    /**
     * 强制选择器重新评估所有候选路线并重新选择其中的最佳路线。
     */
    private fun reselect() {

        selectedRoute = table.invalidRoute
        selectedNeighbor = null

        table.forEach { neighbor, route, enabled ->
            if (enabled && compare(route, selectedRoute) > 0) {
                selectedRoute = route
                selectedNeighbor = neighbor
            }
        }
    }

    /**
     * 清除底层路由表中的所有路由。选择器自动将其选择的路由更新为无效路由。
     */
    fun clear() {
        selectedRoute = table.invalidRoute
        selectedNeighbor = null
        table.clear()
        mutableDisabledNeighbors.clear()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun updateSelectedTo(route: R, neighbor: Node<R>?) {
        selectedRoute = route
        selectedNeighbor = neighbor
    }

    override fun toString(): String {
        return "RouteSelector(table=$table)"
    }

}