import sys

def process(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Imports
    content = content.replace("import { heroImage, seedCatalogProducts, seckillProducts } from '../catalog/productAssets';", "import { heroImage, seedCatalogProducts, seckillProducts, onlineProductImage } from '../catalog/productAssets';\nimport ProductImage from '../components/ProductImage.vue';")
    
    # getImage function
    old_getImage = """function getImage(product: UnknownRecord) {
  return String(product.mainImage || 'https://picsum.photos/400');
}"""
    new_getImage = """function getImage(product: UnknownRecord) {
  return onlineProductImage(product);
}"""
    content = content.replace(old_getImage, new_getImage)

    # Img tags
    content = content.replace('<img :src="getImage(product)" :alt="String(product.name)" />', '<ProductImage :src="getImage(product)" :alt="String(product.name)" />')
    content = content.replace('<img :src="product.mainImage" :alt="product.name" />', '<ProductImage :src="product.mainImage" :alt="product.name" />')

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

process('mall-frontend/src/views/HomeView.vue')
