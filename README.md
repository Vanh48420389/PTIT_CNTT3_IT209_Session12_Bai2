# BÀI TẬP 2: TỰ ĐỘNG HÓA ĐÓNG GÓI VÀ PHÁT HÀNH DOCKER IMAGE KHI GẮN GIT TAG (v*.*.*)

**Học phần:** Điện toán đám mây / CI-CD & DevOps (IT209)  
**Session:** 12 - Tối ưu hóa & Nâng cao CI/CD Pipeline với GitHub Actions  
**Thư mục bài làm:** `Lession12/Bai02`  
**GitHub Submission Path:** `homework/session_12/ex2/`  

---

## 1. Mục tiêu bài thực hành
- Thiết lập quy trình Phân phối/Triển khai liên tục (CD - Continuous Delivery) tự động đóng gói và phát hành phiên bản Docker Image chính thức khi có sự kiện gán nhãn phát hành (**Git Tag `v*.*.*`**).
- Tự động hóa việc phân tích và tạo thẻ Docker Tag tương ứng với số phiên bản Git Tag bằng công cụ chuyên dụng **`docker/metadata-action@v5`**.
- Đăng nhập và đẩy (push) Docker Image lên kho lưu trữ **GitHub Container Registry (GHCR - `ghcr.io`)** an toàn bằng token tự sinh `secrets.GITHUB_TOKEN`.
- Đảm bảo tuân thủ tiêu chuẩn Semantic Versioning (SemVer) trong quy trình Release Management chuyên nghiệp.

---

## 2. Cấu trúc thư mục dự án

```text
Bai02/
├── .github/
│   └── workflows/
│       └── release.yml          # Kịch bản GitHub Actions CD tự động build & push Docker Image khi gắn Git Tag
├── gradle/
│   └── wrapper/                 # Gradle Wrapper
├── src/
│   ├── main/java/               # Mã nguồn ứng dụng Spring Boot
│   └── test/java/               # Bộ Unit Test JUnit 5
├── Dockerfile                   # Kịch bản Multi-stage Build tối ưu kích thước image
├── docker-compose.yml           # Khởi chạy cục bộ
├── build.gradle                 # Cấu hình Gradle build & dependencies
├── settings.gradle              # Tên định danh dự án
├── gradlew                      # Gradle wrapper script cho Linux/macOS
├── gradlew.bat                  # Gradle wrapper script cho Windows
├── .dockerignore                # Danh sách loại trừ khi đóng gói Docker
├── .gitignore                   # Danh sách loại trừ của Git
└── README.md                    # Tài liệu hướng dẫn chi tiết & báo cáo thực hành
```

---

## 3. Nội dung tệp kịch bản `.github/workflows/release.yml`

```yaml
name: Release Docker Image

on:
  push:
    tags:
      - 'v*.*.*'

permissions:
  contents: read
  packages: write

jobs:
  release-image:
    name: Build & Release Docker Image
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v5

      - name: Chuyển tên Repository về chữ thường (lowercase)
        run: |
          echo "REPO_LC=${GITHUB_REPOSITORY,,}" >> $GITHUB_ENV

      - name: Đăng nhập GitHub Container Registry (GHCR)
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Trích xuất metadata và Docker tags từ Git
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ghcr.io/${{ env.REPO_LC }}
          tags: |
            type=ref,event=tag
            type=semver,pattern={{version}}
            type=semver,pattern={{major}}.{{minor}}
            type=raw,value=latest

      - name: Đóng gói và push Docker Image lên ghcr.io
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
```

---

## 4. Phân tích kỹ thuật chuyên sâu

### 4.1. Sự kiện kích hoạt theo Git Tag (`on.push.tags`)
```yaml
on:
  push:
    tags:
      - 'v*.*.*'
```
- **Nguyên lý:** Workflow sẽ **không** bị kích hoạt khi thực hiện các lệnh `git push` thông thường vào các nhánh (`main`, `dev`, `feature/*`), giúp tiết kiệm tài nguyên CI/CD và tránh việc phát hành các bản build chưa ổn định.
- Kịch bản chỉ kích hoạt khi phát hiện một Git Tag mới đẩy lên GitHub khớp với biểu thức khớp mẫu `v*.*.*` (ví dụ: `v1.0.0`, `v1.2.3`, `v2.0.1`).

### 4.2. Cấu hình phân quyền bảo mật (`permissions`)
```yaml
permissions:
  contents: read
  packages: write
```
- Khi đẩy gói lên GitHub Container Registry (`ghcr.io`), runner cần quyền ghi vào phạm vi GitHub Packages của repository.
- Khai báo `packages: write` cho phép `GITHUB_TOKEN` có thẩm quyền tạo mới hoặc cập nhật package Docker trên GHCR mà không cần cấu hình thêm Personal Access Token (PAT) thủ công.

### 4.3. Xử lý chuẩn hóa tên Image (`REPO_LC`)
```bash
echo "REPO_LC=${GITHUB_REPOSITORY,,}" >> $GITHUB_ENV
```
- Quy chuẩn tên Docker Image quốc tế bắt buộc phải viết ở dạng **chữ thường (lowercase)** (ví dụ: `vanh48420389/ptit_cntt3_it209_session12_bai2`).
- Nếu tài khoản hoặc tên repository có chứa chữ in hoa (chẳng hạn `Vanh48420389`), cú pháp bash `${GITHUB_REPOSITORY,,}` sẽ tự động chuyển toàn bộ về chữ thường, ngăn ngừa lỗi `invalid reference format: repository name must be lowercase`.

### 4.4. Trích xuất thẻ phiên bản tự động bằng `docker/metadata-action@v5`
```yaml
- name: Trích xuất metadata và Docker tags từ Git
  id: meta
  uses: docker/metadata-action@v5
  with:
    images: ghcr.io/${{ env.REPO_LC }}
    tags: |
      type=ref,event=tag
      type=semver,pattern={{version}}
      type=semver,pattern={{major}}.{{minor}}
      type=raw,value=latest
```
Khi người dùng gắn tag `v1.0.0` và đẩy lên GitHub:
- `type=ref,event=tag`: Tạo thẻ nguyên bản theo Git Tag `v1.0.0` $\rightarrow$ `ghcr.io/...:v1.0.0`.
- `type=semver,pattern={{version}}`: Trích xuất số phiên bản chuẩn SemVer `1.0.0` $\rightarrow$ `ghcr.io/...:1.0.0`.
- `type=semver,pattern={{major}}.{{minor}}`: Trích xuất phiên bản rút gọn `1.0` $\rightarrow$ `ghcr.io/...:1.0`.
- `type=raw,value=latest`: Cập nhật thẻ `latest` trỏ đến bản phát hành mới nhất này $\rightarrow$ `ghcr.io/...:latest`.
- Toàn bộ danh sách thẻ và nhãn OpenContainers (OCI Labels) được xuất ra `steps.meta.outputs.tags` và `steps.meta.outputs.labels`.

### 4.5. Đóng gói & Phát hành với `docker/build-push-action@v5`
```yaml
- name: Đóng gói và push Docker Image lên ghcr.io
  uses: docker/build-push-action@v5
  with:
    context: .
    push: true
    tags: ${{ steps.meta.outputs.tags }}
    labels: ${{ steps.meta.outputs.labels }}
```
- Đọc `Dockerfile` tại thư mục gốc (`context: .`), thực thi Multi-stage build:
  - **Stage 1 (Builder):** Sử dụng `eclipse-temurin:21-jdk-alpine` biên dịch mã nguồn Java Spring Boot thành tệp thực thi `.jar`.
  - **Stage 2 (Runtime):** Sử dụng JRE siêu nhẹ `eclipse-temurin:21-jre-alpine` và chạy dưới quyền non-root `appuser`.
- Đẩy trực tiếp image đã đóng gói lên `ghcr.io` với đầy đủ các tag đã trích xuất từ bước metadata.

---

## 5. Hướng dẫn các bước thực hiện và kiểm tra

### Bước 1: Khởi tạo Git & Cam kết mã nguồn tại `Lession12/Bai02`
Mở PowerShell/Terminal tại thư mục `d:/IT209/Lession12/Bai02`:
```powershell
git init -b main
git add .
git commit -m "feat: setup release workflow for automated docker publishing to GHCR on git tag"
```

### Bước 2: Thiết lập Remote Repository trên GitHub
Tạo repository mới trên GitHub (ví dụ: `PTIT_CNTT3_IT209_Session12_Bai2`) và liên kết:
```powershell
git remote add origin https://github.com/Vanh48420389/PTIT_CNTT3_IT209_Session12_Bai2.git
git push -u origin main
```

*(Lúc này, nhánh `main` được đẩy lên nhưng workflow `release.yml` **chưa chạy** do chưa có sự kiện push tag).*

### Bước 3: Tạo Git Tag và đẩy Tag lên GitHub để kích hoạt Release
Thực thi lệnh gán tag local và đẩy lên GitHub:
```powershell
git tag v1.0.0
git push origin v1.0.0
```

*(Hoặc gộp chung thành một lệnh: `git tag v1.0.0 && git push origin v1.0.0`)*

### Bước 4: Kiểm tra kết quả thực thi
1. **Kiểm tra tab Actions:**
   - Truy cập vào repository GitHub, mở tab **Actions**.
   - Quan sát thấy workflow **`Release Docker Image`** được tự động kích hoạt bởi tag `v1.0.0`.
   - Job **`release-image`** thực hiện tuần tự các bước: Checkout $\rightarrow$ Login GHCR $\rightarrow$ Trích xuất metadata $\rightarrow$ Build & Push Image thành công với tích xanh (**Success**).

2. **Kiểm tra GitHub Packages:**
   - Vào trang chính của Repository hoặc trang cá nhân GitHub, truy cập mục **Packages**.
   - Quan sát thấy gói Docker Image xuất hiện với tên:
     ```text
     ghcr.io/vanh48420389/ptit_cntt3_it209_session12_bai2:v1.0.0
     ```
   - Đi kèm với các thẻ: `v1.0.0`, `1.0.0`, `1.0`, `latest`.

---

## 6. Hướng dẫn nộp bài
- **Sản phẩm nộp bài:**
  1. Tệp kịch bản workflow: `.github/workflows/release.yml`
  2. Ảnh chụp màn hình:
     - Workflow `release.yml` chạy thành công (tích xanh) trên tab **Actions**.
     - Gói Docker Image phiên bản `v1.0.0` hiển thị trong mục **GitHub Packages** / **Packages** của repository.
- **Thư mục nộp bài quy định:** `homework/session_12/ex2/`
