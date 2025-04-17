rm -rf ./dist
mkdir -p dist
# 构建前端
cd front-end && vite build --emptyOutDir --outDir ../dist/web
