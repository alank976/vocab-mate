extern crate cfg_if;
extern crate wasm_bindgen;

mod app;
mod configs;
mod utils;

use cfg_if::cfg_if;
use std::collections::HashMap;
use wasm_bindgen::prelude::*;

use crate::app::{create_app_context, prelude::*};

use crate::configs::Configs;
use crate::utils::{self, log};
use serde::{Deserialize, Serialize};

/*
This main `lib` & `utils` module should be the only ones hold wasm related I/O logic
*/

cfg_if! {
    // When the `wee_alloc` feature is enabled, use `wee_alloc` as the global
    // allocator.
    if #[cfg(feature = "wee_alloc")] {
        extern crate wee_alloc;
        #[global_allocator]
        static ALLOC: wee_alloc::WeeAlloc = wee_alloc::WeeAlloc::INIT;
    }
}

#[wasm_bindgen]
pub async fn handle_request(config: JsValue, request: JsValue) -> Result<JsValue, JsValue> {
    let application_configs: Configs = config.into_serde().unwrap();
    let request: RequestFromJs = request.into_serde().unwrap();
    let path_variable = request.url.split("/").last().filter(|x| !x.is_empty());
    log::info(&format!("{:?}", path_variable));

    match path_variable {
        Some(vocab) if !vocab.ends_with(".ico") => {
            let vocabs = app::api::lookup(application_configs, vocab).await;
            Ok(JsValue::from_serde(vocabs))
        }
        _ => no_op(),
    }
}

fn no_op() -> Result<JsValue, JsValue> {
    Ok(JsValue::NULL)
}

/// https://developers.cloudflare.com/workers/runtime-apis/request
#[derive(Serialize, Deserialize, Debug)]
struct RequestFromJs {
    pub url: String,
    pub method: String,
    pub redirect: String,
    // TODO: https://rustwasm.github.io/wasm-bindgen/reference/working-with-duck-typed-interfaces.html & https://developer.mozilla.org/en-US/docs/Web/API/Headers
    // pub headers: HashMap<String, String>,
    pub body: Option<String>,
}

#[wasm_bindgen(start)]
pub fn init() {
    utils::set_panic_hook();
}
