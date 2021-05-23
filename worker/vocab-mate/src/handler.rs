use std::collections::HashMap;

use crate::app::{create_app_context, prelude::*};
use wasm_bindgen::prelude::*;

use crate::configs::Configs;
use crate::utils::{self, log};
use serde::{Deserialize, Serialize};
use web_sys::console;

/// https://developers.cloudflare.com/workers/runtime-apis/request
#[derive(Serialize, Deserialize, Debug)]
pub struct RequestFromJs {
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

#[wasm_bindgen]
pub async fn handle_request(config: JsValue, request: JsValue) -> Result<JsValue, JsValue> {
    let application_configs: Configs = config.into_serde().unwrap();
    let request: RequestFromJs = request.into_serde().unwrap();
    let context = create_app_context(application_configs);
    let path_variable = request.url.split("/").last().filter(|x| !x.is_empty());
    log::info(&format!("{:?}", path_variable));

    let js_value = match path_variable {
        // TODO: regex for words only
        Some(vocab) if !vocab.ends_with(".ico") => {
            let vocabs = context
                .dict
                .async_lookup(vocab.to_string())
                .await
                .expect("failed to async lookup");
            JsValue::from_serde(&vocabs).unwrap()
        }
        _ => JsValue::NULL,
    };
    Ok(js_value)
}
