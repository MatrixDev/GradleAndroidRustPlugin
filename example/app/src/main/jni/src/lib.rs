use jni::{JNIEnv, objects::JObject};
use jni::sys::jstring;

#[unsafe(no_mangle)]
pub extern "C" fn Java_dev_rodroid_rust_MainActivity_callRustCode(
    env: JNIEnv,
    _: JObject,
) -> jstring {
    let message = "Hello from Rust";
    let java_string = env.new_string(message).expect("Couldn't create java string!");
    java_string.into_inner()
}