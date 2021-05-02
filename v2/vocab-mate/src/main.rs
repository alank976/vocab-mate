use actix_web::{web, App, HttpServer};

mod api;
mod vocab;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    HttpServer::new(|| {
        App::new()
            .service(api::hello)
            .service(api::echo)
            .route("/hey", web::get().to(api::manual_hello))
            .service(api::get_vocab)
    })
    .bind("127.0.0.1:8080")?
    .run()
    .await
}
