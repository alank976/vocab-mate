fn main() {
    println!("cargo:rerun-if-changed=graphql/query.graphql");
    println!("cargo:rerun-if-changed=graphql/schema.graphql");
    println!("cargo:rerun-if-changed=graphql/downloaded-schema.graphql");
    println!("cargo:rerun-if-changed=graphql/schema.json");
}