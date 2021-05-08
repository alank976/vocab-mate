use actix_web::{
    error::ErrorInternalServerError,
    get,
    web::{Data, Path},
    HttpResponse, Responder,
};

use crate::{dict, prelude::*, AppContext};

#[get("/vocabs/{vocab}")]
pub async fn get_vocab(
    vocab: Path<String>,
    state: Data<AppContext<dict::DictImpl>>,
) -> impl Responder {
    state
        .dict
        .look_up(vocab.into_inner())
        .map(|vocabs| HttpResponse::Ok().json(vocabs))
        .map_err(|e| ErrorInternalServerError(e))
}
