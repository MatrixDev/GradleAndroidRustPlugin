use jni::JNIEnv;
use jni::objects::JObject;
use jni::sys::jstring;

#[no_mangle]
extern fn Java_dev_matrix_rust_MainActivity_callRustCode(env: JNIEnv, _: JObject) -> jstring {
    env.new_string("Hello from rust!").unwrap().into_inner()
}

#[cfg(test)]
mod test {
    #[test]
    fn test() {
        println!("Hello from rust test!")
    }
}
