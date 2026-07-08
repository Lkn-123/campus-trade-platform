<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="always">
      <h2 class="auth-title">校园二手交易平台</h2>
      <p class="auth-subtitle">用户登录</p>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0" size="large">
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="手机号 / 管理员账号" prefix-icon="Iphone" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="auth-btn" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        还没有账号？<router-link to="/register">立即注册</router-link>
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
const form = reactive({ account: "", password: "" });
const rules = {
  account: [{ required: true, message: "请输入手机号或账号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => {});
  if (!valid) return;
  loading.value = true;
  try {
    await userStore.login(form);
    ElMessage.success("登录成功");
    router.push(userStore.isAdmin ? "/admin/dashboard" : "/home");
  } catch (e) {
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; align-items: center; min-height: calc(100vh - 112px); }
.auth-card { width: 420px; border-radius: 12px; box-shadow: 0 4px 16px 0 rgb(0 0 0 / 0.06), 0 2px 4px -2px rgb(0 0 0 / 0.08); padding: 20px; }
.auth-title { text-align: center; color: var(--el-color-primary); margin-bottom: 4px; font-size: 22px; }
.auth-subtitle { text-align: center; color: var(--el-text-color-secondary); margin-bottom: 32px; font-size: 14px; }
.auth-btn { width: 100%; border-radius: var(--el-border-radius-base); }
.auth-footer { text-align: center; font-size: 14px; color: var(--el-text-color-secondary); margin-top: 8px; }
.auth-footer a { color: var(--el-color-primary); text-decoration: none; font-weight: 500; }
</style>
