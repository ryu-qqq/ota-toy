rootProject.name = "ota-toy"

// Core Modules
include("domain")
include("application")

// Adapter Modules (Inbound)
include("adapter-in:rest-api-core")
include("adapter-in:rest-api-extranet")
include("adapter-in:rest-api-customer")
include("adapter-in:rest-api-admin")

// Adapter Modules (Outbound - Persistence)
include("adapter-out:persistence-mysql")
include("adapter-out:persistence-redis")

// Bootstrap Modules (Spring Boot Application)
include("bootstrap:bootstrap-extranet")
include("bootstrap:bootstrap-customer")
include("bootstrap:bootstrap-admin")

// Project directory mappings
project(":adapter-in:rest-api-core").projectDir = file("adapter-in/rest-api-core")
project(":adapter-in:rest-api-extranet").projectDir = file("adapter-in/rest-api-extranet")
project(":adapter-in:rest-api-customer").projectDir = file("adapter-in/rest-api-customer")
project(":adapter-in:rest-api-admin").projectDir = file("adapter-in/rest-api-admin")
project(":adapter-out:persistence-mysql").projectDir = file("adapter-out/persistence-mysql")
project(":adapter-out:persistence-redis").projectDir = file("adapter-out/persistence-redis")
project(":bootstrap:bootstrap-extranet").projectDir = file("bootstrap/bootstrap-extranet")
project(":bootstrap:bootstrap-customer").projectDir = file("bootstrap/bootstrap-customer")
project(":bootstrap:bootstrap-admin").projectDir = file("bootstrap/bootstrap-admin")
