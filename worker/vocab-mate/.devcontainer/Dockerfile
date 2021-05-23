# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.177.0/containers/rust/.devcontainer/base.Dockerfile

FROM mcr.microsoft.com/vscode/devcontainers/rust:0-1

# [Optional] Uncomment this section to install additional packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

RUN cargo install wrangler
# TODO
# RUN echo 'api_token = "taEa3C2BxVL1lg9m_J2Tzu_Upzn-kGcTzEKBg9aM"' > "$HOME/.wrangler/config/default.toml"