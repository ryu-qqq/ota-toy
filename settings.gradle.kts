rootProject.name = "ota-toy"

// Core Modules
include("domain")
include("application")

// Adapter Modules (Inbound)
include("adapter-in:rest-api")

// Adapter Modules (Outbound - Persistence)
include("adapter-out:persistence-mysql")
include("adapter-out:persistence-redis")

// Project directory mappings
project(":adapter-in:rest-api").projectDir = file("adapter-in/rest-api")
project(":adapter-out:persistence-mysql").projectDir = file("adapter-out/persistence-mysql")
project(":adapter-out:persistence-redis").projectDir = file("adapter-out/persistence-redis")
