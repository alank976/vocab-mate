use actix_web::{get, post, web, HttpResponse, Responder};

use crate::vocab::*;

#[get("/")]
pub async fn hello() -> impl Responder {
    HttpResponse::Ok().body("Hello world!")
}

#[post("/echo")]
pub async fn echo(req_body: String) -> impl Responder {
    HttpResponse::Ok().body(req_body)
}

pub async fn manual_hello() -> impl Responder {
    HttpResponse::Ok().body("Hey there!")
}

#[get("/vocabs/{vocab}")]
pub async fn get_vocab(vocab: web::Path<String>) -> impl Responder {
    HttpResponse::Ok().json(vec![Vocab::new(
        "a".into(),
        vocab.into_inner(),
        PartOfSpeech::Verb,
        "temp".into(),
    )])
}
