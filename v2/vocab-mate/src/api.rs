use actix_web::{
    error::ErrorInternalServerError,
    get,
    web::{self, Data},
    HttpResponse, Responder,
};

use crate::{prelude::*, AppContext};

use crate::fauna;

#[get("/vocabs/{vocab}")]
pub async fn get_vocab(
    vocab: web::Path<String>,
    state: Data<AppContext<fauna::FaunaDbClient>>,
) -> impl Responder {
    state
        .dict
        .look_up(vocab.into_inner())
        .map(|vocabs| HttpResponse::Ok().json(vocabs))
        .map_err(|e| ErrorInternalServerError(e))
}
