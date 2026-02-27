#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "$0" )" && pwd )"

# 생성 파일을 Service 폴더로
GEN_CPP_DIR="${SCRIPT_DIR}/../Service"

mkdir -p "${GEN_CPP_DIR}"

PROTO_FILE="${SCRIPT_DIR}/Extension.proto"

protoc \
  -I="${SCRIPT_DIR}" \
  --cpp_out="${GEN_CPP_DIR}" \
  --grpc_out="${GEN_CPP_DIR}" \
  --plugin=protoc-gen-grpc="$(which grpc_cpp_plugin)" \
  "${PROTO_FILE}"
