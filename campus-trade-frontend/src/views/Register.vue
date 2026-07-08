<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="always">
      <h2 class="auth-title">校园二手交易平台</h2>
      <p class="auth-subtitle">用户注册</p>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0" size="large">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" prefix-icon="Iphone" maxlength="11" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（至少6位）" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="auth-btn" :loading="loading" @click="handleRegister">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useUserStore } from "../stores/user";

const router = useRouter();
const userStore = useUserStore();
const formRef = ref(null);
const loading = ref(false);
const form = reactive({ phone: "", password: "", confirmPassword: "" });
const validatePass2 = (rule, value, callback) => {
  if (value !== form.password) callback(new Error("两次密码输入不一致"));
  else callback();
};
const rules = {
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" },
  ],
  password: [{ required: true, min: 6, message: "密码至少6位", trigger: "blur" }],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: "blur" }],
};

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => {});
  if (!valid) return;
  loading.value = true;
  try {
    await userStore.register({ phone: form.phone, password: form.password });
    ElMessage.success("注册成功，请登录");
    router.push("/login");
   } catch (e) { ElMessage.error(e?.response?.data?.message || e?.message || "注册失败"); }
  finally { loading.value = false; }
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; align-items: center; min-height: calc(100vh - 100px); }
.auth-card { width: 420px; padding: 20px; }
.auth-title { text-align: center; color: #409eff; margin-bottom: 5px; }
.auth-subtitle { text-align: center; color: #909399; margin-bottom: 30px; font-size: 14px; }
.auth-btn { width: 100%; }
.auth-footer { text-align: center; font-size: 14px; color: #909399; }
.auth-footer a { color: #409eff; text-decoration: none; }
</style>
