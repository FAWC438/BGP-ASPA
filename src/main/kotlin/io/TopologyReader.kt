package io

import core.routing.Route
import core.routing.Topology

interface TopologyReader<R : Route> {

    fun read(): Topology<R>
}