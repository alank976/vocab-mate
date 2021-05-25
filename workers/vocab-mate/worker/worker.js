addEventListener('fetch', event => {
  event.respondWith(handleRequest(event.request))
})


/**
 * Fetch and log a request
 * @param {Request} request
 */
async function handleRequest(request) {
  const configs = {
    faunadb: {
      url: FAUNADB_URL,
      api_key: FAUNADB_API_KEY
    },
    rapidapi: {
      wordsapi_url: RAPIDAPI_WORDSAPI_URL,
      api_key_header: RAPIDAPI_API_KEY_HEADER,
      api_key: RAPIDAPI_API_KEY
    },
    vocab_mate: {
      expiry: VOCAB_MATE_EXPIRY
    }
  };
  const { handle_request } = wasm_bindgen;
  await wasm_bindgen(wasm)
  let response = await handle_request(configs, request);
  return new Response(JSON.stringify(response), { status: 200 })
}
