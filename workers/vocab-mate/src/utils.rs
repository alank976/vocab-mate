#[allow(unused_imports)]
use wasm_bindgen::prelude::*;

use cfg_if::cfg_if;

cfg_if! {
    // When the `console_error_panic_hook` feature is enabled, we can call the
    // `set_panic_hook` function at least once during initialization, and then
    // we will get better error messages if our code ever panics.
    //
    // For more details see
    // https://github.com/rustwasm/console_error_panic_hook#readme
    if #[cfg(feature = "console_error_panic_hook")] {
        extern crate console_error_panic_hook;
        pub use self::console_error_panic_hook::set_once as set_panic_hook;
    } else {
        #[inline]
        pub fn set_panic_hook() {}
    }
}

#[allow(unused_unsafe)]
pub mod log {
    use wasm_bindgen::JsValue;
    use web_sys::console::{debug_1, error_1, info_1};

    pub fn info(message: &str) {
        unsafe { info_1(&JsValue::from_str(message)) }
    }

    pub fn debug(message: &str) {
        unsafe { debug_1(&JsValue::from_str(message)) }
    }
    pub fn error(message: &str) {
        unsafe { error_1(&JsValue::from_str(message)) }
    }
}
