<template>
  <div id="app">
    <router-view />
    <ModelDialog />
  </div>
</template>

<script setup>
import ModelDialog from './components/ModelDialog.vue'
</script>

<style>
@import './styles/global.css';
</style>
