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
.auth-page { display: flex; justify-content: center; align-items: center; min-height: calc(100vh - 100px); }
.auth-card { width: 420px; padding: 20px; }
.auth-title { text-align: center; color: #409eff; margin-bottom: 5px; }
.auth-subtitle { text-align: center; color: #909399; margin-bottom: 30px; font-size: 14px; }
.auth-btn { width: 100%; }
.auth-footer { text-align: center; font-size: 14px; color: #909399; }
.auth-footer a { color: #409eff; text-decoration: none; }
</style>
