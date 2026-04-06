rootProject.name = "ota-toy"

// Core Modules
include("domain")
include("application")

// Adapter Modules (Inbound)
include("adapter-in:rest-api-core")
include("adapter-in:rest-api-extranet")
include("adapter-in:rest-api-customer")
include("adapter-in:rest-api-admin")
include("adapter-in:scheduler")

// Adapter Modules (Outbound - Persistence)
include("adapter-out:persistence-mysql")
include("adapter-out:persistence-redis")

// Adapter Modules (Outbound - Client)
include("adapter-out:client:supplier-client")

// Bootstrap Modules (Spring Boot Application)
include("bootstrap:bootstrap-extranet")
include("bootstrap:bootstrap-customer")
include("bootstrap:bootstrap-admin")
include("bootstrap:bootstrap-scheduler")

// Project directory mappings
project(":adapter-in:rest-api-core").projectDir = file("adapter-in/rest-api-core")
project(":adapter-in:rest-api-extranet").projectDir = file("adapter-in/rest-api-extranet")
project(":adapter-in:rest-api-customer").projectDir = file("adapter-in/rest-api-customer")
project(":adapter-in:rest-api-admin").projectDir = file("adapter-in/rest-api-admin")
project(":adapter-in:scheduler").projectDir = file("adapter-in/scheduler")
project(":adapter-out:persistence-mysql").projectDir = file("adapter-out/persistence-mysql")
project(":adapter-out:persistence-redis").projectDir = file("adapter-out/persistence-redis")
project(":adapter-out:client:supplier-client").projectDir = file("adapter-out/client/supplier-client")
project(":bootstrap:bootstrap-extranet").projectDir = file("bootstrap/bootstrap-extranet")
project(":bootstrap:bootstrap-customer").projectDir = file("bootstrap/bootstrap-customer")
project(":bootstrap:bootstrap-admin").projectDir = file("bootstrap/bootstrap-admin")
project(":bootstrap:bootstrap-scheduler").projectDir = file("bootstrap/bootstrap-scheduler")
