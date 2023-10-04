import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:richminime/constants/api.dart';
import 'package:richminime/models/clothing_model.dart';
import 'package:http_interceptor/http_interceptor.dart';
import 'package:cookie_jar/cookie_jar.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:richminime/interceptor/interceptor.dart';

const storage = FlutterSecureStorage();
const String baseUrl = Api.BASE_URL;

class ClothingService {
  final client = InterceptedClient.build(interceptors: [HttpInterceptor()]);

  // 전체 옷 조회(카테고리 별) - 옷가게
  Future<List<ClothingModel>> getAllClothings(String? clothingType) async {
    List<ClothingModel> clothingInstances = [];

    final url = Uri.parse('$baseUrl/clothing?clothingType=$clothingType');
    final token = await storage.read(key: "accessToken");
    final headers = {
      'Authorization': 'Bearer $token', // accessToken을 헤더에 추가
    };

    final response = await client.get(
      url,
      headers: headers, // 헤더 추가
    );
    print(response.statusCode);
    print(response.body);
    if (response.statusCode == 200) {
      // message, data형태로 담겨있는 response를 우선 responseBody로 받은 다음
      final Map<String, dynamic> responseBody = jsonDecode(response.body);
      // 거기서 data만 뽑아서 clothing에 담아줌.
      final List<dynamic> clothings = jsonDecode(responseBody['data']);
      for (var clothing in clothings) {
        clothingInstances.add(ClothingModel.fromJson(clothing));
      }

      return clothingInstances;
    } else if (response.statusCode == 401 || response.statusCode == 500) {
      return getAllClothings(clothingType);
    }
    throw Error();
  }

  // 옷 구매
  Future buyClothing(int clothingId, int price) async {
    final url = Uri.parse('$baseUrl/clothing/my/$clothingId');

    final response = await client.post(url, headers: {
      //  "Authorization": "Bearer $accessToken",
    });

    if (response.statusCode == 200) {}
    throw Error();
  }

  // 옷 팔기(삭제)
  Future sellClothing(int clothingId) async {
    final url = Uri.parse('$baseUrl/clothing/my/$clothingId');

    final response = await client.delete(url, headers: {
      //  "Authorization": "Bearer $accessToken",
    });

    if (response.statusCode == 200) {
      //삭제된 옷장 재 렌더링(response로 보내줌)
    }
    throw Error();
  }

  // 내가 소유한 옷 전체 불러오기-my
  Future<List<ClothingModel>> getMyAllClothings(String? clothingType) async {
    List<ClothingModel> myClothingInstances = [];

    final url = Uri.parse('$baseUrl/clothing/my?clothingType=$clothingType');

    final response = await client.get(url);

    if (response.statusCode == 200) {
      // message, data형태로 담겨있는 response를 우선 responseBody로 받은 다음
      final Map<String, dynamic> responseBody = jsonDecode(response.body);
      // 거기서 data만 뽑아서 clothing에 담아줌.
      final List<dynamic> clothings = jsonDecode(responseBody['data']);
      for (var clothing in clothings) {
        myClothingInstances.add(ClothingModel.fromJson(clothing));
      }

      return myClothingInstances;
    }
    throw Error();
  }
}
